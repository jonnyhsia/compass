package com.arch.jonnyhsia.compass.compiler;

import com.arch.jonnyhsia.compass.facade.CompassPage;
import com.arch.jonnyhsia.compass.facade.ICompassTable;
import com.arch.jonnyhsia.compass.facade.annotation.Route;
import com.arch.jonnyhsia.compass.facade.TargetType;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class CompassProcessor extends AbstractProcessor {

    private final static String TABLE_FULL_PATH = "compassTable";
    private final static String DEFAULT_SCHEME = "compassDefaultPageScheme";

    private Filer filer;
    private Messager messager;

    private String tablePackage;
    private String tableName;
    private String defaultScheme;

    private Elements elementUtils;
    private Types typeUtils;

    private TypeMirror typeActivity;
    private TypeMirror typeFragment;

    private int round = 0;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();

        typeActivity = elementUtils.getTypeElement(Const.ACTIVITY).asType();
        typeFragment = elementUtils.getTypeElement(Const.FRAGMENT).asType();

        final String tableFullName = processingEnvironment.getOptions().get(TABLE_FULL_PATH);
        if (tableFullName != null && !tableFullName.isEmpty()) {
            final int indexOfLastPeriod = tableFullName.lastIndexOf(".");
            tablePackage = tableFullName.substring(0, indexOfLastPeriod);
            tableName = tableFullName.substring(indexOfLastPeriod + 1);
        } else {
            tablePackage = "com.arch.jonnyhsia.compass";
            tableName = "CompassTable";
        }

        final String scheme = processingEnvironment.getOptions().get(DEFAULT_SCHEME);
        if (scheme != null && !scheme.isEmpty()) {
            defaultScheme = scheme;
        } else {
            defaultScheme = "app";
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        try {
            note("Hello from the other side...");
            // 跳过重复执行的 process
            if (annotations == null || annotations.isEmpty()) {
                note("No Annotations... Generate an empty table.");
                return true;
            }
            if (round > 0) {
                return false;
            }
            round++;

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
        // HashMap<PageKey, CompassPage>
        ParameterizedTypeName pageMapType = ParameterizedTypeName.get(HashMap.class, String.class, CompassPage.class);

        // @Override
        // final Map<PageKey, CompassPage> loadPages()
        // HashMap<PageKey, CompassPage> map = new HashMap<>()
        MethodSpec.Builder getPagesMethod = MethodSpec.methodBuilder("getPages")
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .returns(pageMapType)
                .addStatement("$T map = new $T<>()", pageMapType, HashMap.class);

        // map.put(PageKey, CompassPage)
        for (RouteInfo routeInfo : routeInfoList) {
            TargetType targetType = getTargetTypeByRouteInfo(routeInfo);
            getPagesMethod.addComment(routeInfo.getRouteString());
            getPagesMethod.addCode("map.put($S, new $T($S, $T.class, $L, $T.$L, $S",
                    routeInfo.getName(),
                    CompassPage.class,
                    routeInfo.getName(), routeInfo.getTarget(), routeInfo.getRequestCode(),
                    targetType.getClass(), targetType.name(),
                    pageScheme(routeInfo.getScheme()));

            if (routeInfo.getInterceptors().size() == 0) {
                getPagesMethod.addCode(", new $T[0]", Class.class);
            } else {
                getPagesMethod.addCode(", new $T[] {", Class.class);
                for (int i = 0; i < routeInfo.getInterceptors().size(); i++) {
                    ClassName interceptorClz = routeInfo.getInterceptors().get(i);
                    getPagesMethod.addCode("$T.class", interceptorClz);
                    if (i != routeInfo.getInterceptors().size() - 1) {
                        getPagesMethod.addCode(", ");
                    }
                }
                getPagesMethod.addCode("}");
            }

            getPagesMethod.addStatement("));");
        }
        // return map
        getPagesMethod.addStatement("return map");

        // public final class XXX { }
        TypeSpec tableClass = TypeSpec.classBuilder(tableName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ICompassTable.class)
                .addMethod(getPagesMethod.build())
                .build();

        // com.arch.jonnyhsia.compass.XXX
        JavaFile javaFile = JavaFile.builder(tablePackage, tableClass).build();
        javaFile.writeTo(filer);
    }

    private TargetType getTargetTypeByRouteInfo(final RouteInfo routeInfo) {
        if (typeUtils.isSubtype(routeInfo.getType(), typeActivity)) {
            return TargetType.ACTIVITY;
        } else if (typeUtils.isSubtype(routeInfo.getType(), typeFragment)){
            return TargetType.FRAGMENT;
        }
        return TargetType.UNKNOWN;
    }

    private String pageScheme(String scheme) {
        return scheme.isEmpty() ? defaultScheme : scheme;
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