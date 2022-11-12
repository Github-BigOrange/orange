package generate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author caixr
 * @title: FastGenerator
 * @projectName base
 * @description: 代码生成器
 * @date 2022/4/12 15:03
 */
public class FastGenerator {

    private static final DataSourceConfig.Builder DATA_SOURCE_CONFIG = new DataSourceConfig.Builder(
            "jdbc:mysql://192.168.5.233:3306/safe_wdyj_test?serverTimezone=GMT%2B8&useUnicode=true&useSSL=false&characterEncoding=utf8&rewriteBatchedStatements=true",
            "root",
            "HightopDL@2022MySQL");


    public static void main(String[] args) {
        List<String> tables = new ArrayList<>();
        tables.add("province_wdtower");

        FastAutoGenerator.create(DATA_SOURCE_CONFIG)
                //全局配置
                .globalConfig(builder -> {
                    builder.author("orange")
                            .outputDir(System.getProperty("user.dir")+"\\generate" +"\\src\\main\\java")
                            .enableSwagger()
                            //生成文件后是否打开文件夹
                            .disableOpenDir()
                            .fileOverride();
                })
                //包设置
                .packageConfig(builder -> builder.parent("generate")
                        .moduleName("test")
                        .entity("entity")
                        .service("service.intef")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .mapper("mapper")
                        .xml("mapper.xml")
                        .pathInfo(Collections.singletonMap(OutputFile.mapperXml, System.getProperty("user.dir")+"\\generate" + "\\src\\main\\resources\\mapper")))

                //策略配置
                .strategyConfig(builder -> builder.addInclude(tables)
                        //表前缀
                        .addTablePrefix("t_", "wd_")
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .entityBuilder()
                        .enableLombok()
                        .idType(IdType.ASSIGN_ID)
                        .logicDeleteColumnName("deleted")
                        .enableTableFieldAnnotation()
                        .controllerBuilder()
                        .formatFileName("%sController")
                        .enableRestStyle()
                        .mapperBuilder()
                        .superClass(BaseMapper.class)
                        .formatMapperFileName("%sMapper")
                        .enableMapperAnnotation()
                        .formatXmlFileName("%sMapper"))
                //模板配置
//                .templateConfig(builder -> {
//                    builder.controller("/templates/Controller");
//                    .entity("/templates/entity.ftl")
//                    .mapper("/templates/mapper.ftl");
//                })
                //自定义注入配置
                .injectionConfig(builder -> {
                    builder.beforeOutputFile((tableInfo, objectMap) -> {
                        System.out.println("tableInfo: " + tableInfo.getEntityName() + " objectMap: " + objectMap.size());
                    });
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
