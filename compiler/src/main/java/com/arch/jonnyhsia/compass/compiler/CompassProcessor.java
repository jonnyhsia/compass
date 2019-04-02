package com.arch.jonnyhsia.compass.compiler;

import com.arch.jonnyhsia.compass.api.CompassPage;
import com.arch.jonnyhsia.compass.api.Route;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

@AutoService(Processor.class)
public class CompassProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        try {
            note("Hello from the other side...");
            if (annotations == null || annotations.isEmpty()) {
                // 跳过重复执行的 process
                note("No Annotations... Annotation Process Skipped.");
                return true;
            }

            List<RouteInfo> routeInfoList = new ArrayList<>();
            for (Element annotatedElement : roundEnvironment.getElementsAnnotatedWith(Route.class)) {
                if (annotatedElement.getKind() != ElementKind.CLASS) {
                    // 注解类型错误, 则跳过
                    error("该注解只能用于类", annotatedElement);
                    continue;
                }
                // 将注解信息保存在 RouteInfo 中, 并添加到数组
                routeInfoList.add(new RouteInfo((TypeElement) annotatedElement));
            }

            // 生成 Java Code
            generateJavaCode(routeInfoList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void generateJavaCode(List<RouteInfo> routeInfoList) throws IOException {
        // List<CompassPage>
        ParameterizedTypeName pageListType = ParameterizedTypeName.get(HashMap.class, String.class, CompassPage.class);
        // public static List addPages() {}
        MethodSpec.Builder addPageMethod = MethodSpec.methodBuilder("getPages")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(pageListType)
                .addStatement("$T map = new $T<>()", pageListType, HashMap.class);

        // list.add(new CompassPage(scheme, name, clz[]))
        for (RouteInfo routeInfo : routeInfoList) {
            addPageMethod.addComment(routeInfo.getRouteString());
            addPageMethod.addCode("map.put($S, new $T($S, $T.class, $L",
                    routeInfo.getName(),
                    CompassPage.class, routeInfo.getName(), routeInfo.getTarget(), routeInfo.getRequestCode());

            if (routeInfo.getInterceptors().size() == 0) {
                addPageMethod.addCode(", new $T[0]", Class.class);
            } else {
                addPageMethod.addCode(", new $T[] {", Class.class);
                for (int i = 0; i < routeInfo.getInterceptors().size(); i++) {
                    ClassName interceptorClz = routeInfo.getInterceptors().get(i);
                    addPageMethod.addCode("$T.class", interceptorClz);
                    if (i != routeInfo.getInterceptors().size() - 1) {
                        addPageMethod.addCode(", ");
                    }
                }
                addPageMethod.addCode("}");
            }

            addPageMethod.addStatement("));");
        }
        // return list
        addPageMethod.addStatement("return map");

        // public final class CompassTable { addPages() }
        TypeSpec tableClass = TypeSpec.classBuilder("CompassTable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(addPageMethod.build())
                .build();

        // com.arch.jonnyhsia.compass.CompassTable
        JavaFile javaFile = JavaFile.builder("com.arch.jonnyhsia.compass", tableClass).build();
        javaFile.writeTo(filer);
    }

    private void note(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

    private void error(CharSequence charSequence, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, charSequence, element);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            add(Route.class.getCanonicalName());
        }};
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}