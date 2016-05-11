/*
 *   Copyright 2016 Fabio Collini.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.codingjam.lifecyclebinder;

import android.os.Bundle;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({
        "it.codingjam.lifecyclebinder.LifeCycleAware",
        "it.codingjam.lifecyclebinder.InstanceState"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class LifeCycleBinderProcessor extends AbstractProcessor {

    public static final String LIFE_CYCLE_BINDER_SUFFIX = "$LifeCycleBinder";
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<LifeCycleAwareInfo> elementsByClass = createLifeCycleAwareElements(
                roundEnv.getElementsAnnotatedWith(LifeCycleAware.class),
                roundEnv.getElementsAnnotatedWith(InstanceState.class)
        );

        if (elementsByClass == null) {
            return true;
        }

        calculateNestedElements(elementsByClass);

        for (LifeCycleAwareInfo entry : elementsByClass) {
            generateBinder(entry);
        }
        return false;
    }

    private void calculateNestedElements(List<LifeCycleAwareInfo> elementsByClass) {
        for (LifeCycleAwareInfo lifeCycleAwareInfo : elementsByClass) {
            for (Element element : lifeCycleAwareInfo.lifeCycleAwareElements) {
                for (LifeCycleAwareInfo entry : elementsByClass) {
                    if (entry.element.asType().equals(element.asType())) {
                        lifeCycleAwareInfo.nestedElements.add(new NestedLifeCycleAwareInfo(element, entry, null));
                    }
                }
            }
            for (RetainedObjectInfo retainedEntry : lifeCycleAwareInfo.retainedObjects) {
                for (LifeCycleAwareInfo entry : elementsByClass) {
                    if (ClassName.get(entry.element.asType()).equals(retainedEntry.typeName)) {
                        lifeCycleAwareInfo.nestedElements.add(new NestedLifeCycleAwareInfo(retainedEntry.field, entry, retainedEntry));
                    }
                }
            }
        }
    }

    private List<LifeCycleAwareInfo> createLifeCycleAwareElements(Set<? extends Element> lifeCycleAwareElements, Set<? extends Element> instanceStateElements) {
        Map<Element, LifeCycleAwareInfo> elementsByClass = new HashMap<>();

        for (Element element : lifeCycleAwareElements) {
            if (element.getKind() != ElementKind.FIELD) {
                error(element, "Only fields can be annotated with @%s", LifeCycleAware.class);
                return null;
            }

            VariableElement variable = (VariableElement) element;

            LifeCycleAware annotation = variable.getAnnotation(LifeCycleAware.class);

            TypeElement enclosingElement = (TypeElement) variable.getEnclosingElement();

            LifeCycleAwareInfo info = getLifeCycleAwareInfo(elementsByClass, enclosingElement);
            if (!annotation.retained()) {
                info.lifeCycleAwareElements.add(variable);
            } else {
                TypeName typeName = ((ParameterizedTypeName) ParameterizedTypeName.get(variable.asType())).typeArguments.get(0);
                info.retainedObjects.add(new RetainedObjectInfo(annotation.name(), variable, typeName));
            }
        }
        for (Element element : instanceStateElements) {
            VariableElement variable = (VariableElement) element;
            TypeElement enclosingElement = (TypeElement) variable.getEnclosingElement();
            LifeCycleAwareInfo info = getLifeCycleAwareInfo(elementsByClass, enclosingElement);
            info.instanceStateElements.add(element);
        }
        return new ArrayList<>(elementsByClass.values());
    }

    private LifeCycleAwareInfo getLifeCycleAwareInfo(Map<Element, LifeCycleAwareInfo> elementsByClass, TypeElement enclosingElement) {
        LifeCycleAwareInfo info = elementsByClass.get(enclosingElement);
        if (info == null) {
            info = new LifeCycleAwareInfo(enclosingElement);
            elementsByClass.put(enclosingElement, info);
        }
        return info;
    }

    private void generateBinder(LifeCycleAwareInfo lifeCycleAwareInfo) {
        TypeElement hostElement = lifeCycleAwareInfo.element;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(hostElement);
        final String simpleClassName = hostElement.getSimpleName().toString() + LIFE_CYCLE_BINDER_SUFFIX;
        final String qualifiedClassName = packageElement.getQualifiedName() + "." + simpleClassName;

        try {
            message(Diagnostic.Kind.NOTE, "writing class " + qualifiedClassName);
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                    qualifiedClassName, lifeCycleAwareInfo.lifeCycleAwareElements.toArray(new Element[lifeCycleAwareInfo.lifeCycleAwareElements.size()]));

            TypeName objectGenericType = TypeName.get(hostElement.asType());
            TypeName viewGenericType = getObjectBinderGenericTypeName(hostElement);
            MethodSpec bindMethod = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(objectGenericType, "view")
                    .addCode(generateBindMethod(lifeCycleAwareInfo))
                    .build();

            TypeSpec.Builder builder = TypeSpec.classBuilder(simpleClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(bindMethod)
                    .superclass(ParameterizedTypeName.get(ClassName.get(ObjectBinder.class), objectGenericType, viewGenericType));

            addNestedBinderFields(builder, lifeCycleAwareInfo);

            if (!lifeCycleAwareInfo.instanceStateElements.isEmpty() || !lifeCycleAwareInfo.nestedElements.isEmpty()) {
                builder = builder
                        .addMethod(createSaveInstanceStateMethod(lifeCycleAwareInfo, hostElement.asType()))
                        .addMethod(createRestoreInstanceStateMethod(lifeCycleAwareInfo, hostElement.asType()));
            }

            TypeSpec classType = builder.build();

            final Writer writer = sourceFile.openWriter();
            JavaFile.builder(packageElement.getQualifiedName().toString(), classType)
                    .build()
                    .writeTo(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed writing class file " + qualifiedClassName, e);
        }
    }

    private TypeName getObjectBinderGenericTypeName(TypeElement hostElement) {
        List<? extends TypeMirror> interfaces = hostElement.getInterfaces();
        for (TypeMirror type : interfaces) {
            TypeName typeName = TypeName.get(type);
            if (typeName instanceof ParameterizedTypeName) {
                ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
                if (parameterizedTypeName.typeArguments.size() == 1 && parameterizedTypeName.rawType.equals(TypeName.get(ViewLifeCycleAware.class))) {
                    return parameterizedTypeName.typeArguments.get(0);
                }
            }
        }
        return TypeName.get(hostElement.asType());
    }

    private void addNestedBinderFields(TypeSpec.Builder builder, LifeCycleAwareInfo lifeCycleAwareInfo) {
        for (NestedLifeCycleAwareInfo info : lifeCycleAwareInfo.nestedElements) {
            String typeName;
            if (info.retained != null) {
                typeName = info.retained.typeName.toString();
            } else {
                typeName = info.field.asType().toString();
            }
            ClassName className = ClassName.bestGuess(typeName + LIFE_CYCLE_BINDER_SUFFIX);
            builder.addField(
                    FieldSpec.builder(
                            className,
                            info.field.getSimpleName().toString(),
                            Modifier.PRIVATE
                    ).initializer("new $T()", className).build()
            );
        }
    }

    private MethodSpec createRestoreInstanceStateMethod(LifeCycleAwareInfo lifeCycleAwareInfo, TypeMirror type) {
        return createInstanceStateMethod(lifeCycleAwareInfo, type, "restoreInstanceState", "view.$L = bundle.getParcelable($S)");
    }

    private MethodSpec createSaveInstanceStateMethod(LifeCycleAwareInfo lifeCycleAwareInfo, TypeMirror hostType) {
        return createInstanceStateMethod(lifeCycleAwareInfo, hostType, "saveInstanceState", "bundle.putParcelable($S, view.$L)");
    }

    private MethodSpec createInstanceStateMethod(LifeCycleAwareInfo lifeCycleAwareInfo, TypeMirror type, String methodName, String statement) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(TypeName.get(type), "view")
                .addParameter(Bundle.class, "bundle");
        for (Element element : lifeCycleAwareInfo.instanceStateElements) {
            builder.addStatement(statement, element.getSimpleName(), element.getSimpleName());
        }
        for (NestedLifeCycleAwareInfo info : lifeCycleAwareInfo.nestedElements) {
            if (info.retained != null) {
                builder.addStatement("$L." + methodName + "(($T) retainedObjects.get($S), bundle)", info.field.getSimpleName(), info.retained.typeName, info.retained.name);
            } else {
                builder.addStatement("$L." + methodName + "(view.$L, bundle)", info.field.getSimpleName(), info.field.getSimpleName());
            }
        }
        return builder.build();
    }

    private CodeBlock generateBindMethod(LifeCycleAwareInfo lifeCycleAwareInfo) {
        CodeBlock.Builder builder = CodeBlock.builder();
        for (Element element : lifeCycleAwareInfo.lifeCycleAwareElements) {
            builder.addStatement("listeners.add(view.$L)", element);
        }
        if (!lifeCycleAwareInfo.retainedObjects.isEmpty()) {
            for (RetainedObjectInfo entry : lifeCycleAwareInfo.retainedObjects) {
                builder.addStatement("retainedObjectCallables.put($S, view.$L)", entry.name, entry.field);
            }
        }
        for (NestedLifeCycleAwareInfo info : lifeCycleAwareInfo.nestedElements) {
            if (info.retained != null) {
                builder.addStatement("$L.bind(($T) retainedObjects.get($S))", info.field.getSimpleName(), info.retained.typeName, info.retained.name);
            } else {
                builder.addStatement("$L.bind(view.$L)", info.field.getSimpleName(), info.field.getSimpleName());
            }
            builder.addStatement("listeners.addAll($L.getListeners())", info.field.getSimpleName());
            builder.addStatement("retainedObjectCallables.putAll($L.getRetainedObjectCallables())", info.field.getSimpleName());
        }

        return builder.build();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    private void message(Diagnostic.Kind kind, String message) {
        messager.printMessage(kind, "LifeCycleBinder: " + message);
    }
}
