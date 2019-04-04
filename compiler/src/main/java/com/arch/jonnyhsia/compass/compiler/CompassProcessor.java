package com.arch.jonnyhsia.compass.compiler;

import com.arch.jonnyhsia.compass.api.CompassPage;
import com.arch.jonnyhsia.compass.api.ICompassTable;
import com.arch.jonnyhsia.compass.api.PageKey;
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

    private final static String TABLE_FULL_PATH = "compassTable";
    private final static String DEFAULT_SCHEME = "compassDefaultPageScheme";

    private Filer filer;
    private Messager messager;

    private String tablePackage;
    private String tableName;
    private String defaultScheme;

    private int round = 0;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();

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
        ParameterizedTypeName pageMapType = ParameterizedTypeName.get(HashMap.class, PageKey.class, CompassPage.class);
        // private final static HashMap pages = loadPages();
        FieldSpec.Builder pagesField = FieldSpec.builder(pageMapType, "pages", Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC)
                .initializer("loadPages()");

        // private final static Map loadPages()
        // HashMap map = new HashMap()
        MethodSpec.Builder addPageMethod = MethodSpec.methodBuilder("loadPages")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .returns(pageMapType)
                .addStatement("$T map = new $T<>()", pageMapType, HashMap.class);

        // map.put(PageKey, CompassPage)
        for (RouteInfo routeInfo : routeInfoList) {
            addPageMethod.addComment(routeInfo.getRouteString());
            addPageMethod.addCode("map.put(new $T($S, $S), new $T($S, $T.class, $L",
                    PageKey.class, pageScheme(routeInfo.getScheme()), routeInfo.getName(),
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
        // return map
        addPageMethod.addStatement("return map");

        // public final HashMap getPages() { return pages; }
        MethodSpec.Builder getPagesMethod = MethodSpec.methodBuilder("getPages")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(Override.class)
                .returns(pageMapType)
                .addStatement("return pages");

        // public final class XXX { }
        TypeSpec tableClass = TypeSpec.classBuilder(tableName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ICompassTable.class)
                .addField(pagesField.build())
                .addMethod(addPageMethod.build())
                .addMethod(getPagesMethod.build())
                .build();

        // com.arch.jonnyhsia.compass.XXX
        JavaFile javaFile = JavaFile.builder(tablePackage, tableClass).build();
        javaFile.writeTo(filer);
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