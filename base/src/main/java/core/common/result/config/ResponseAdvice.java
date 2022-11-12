package core.common.result.config;

import core.common.result.vo.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 *
 * 统一返回结构后，在 Controller 中就可以使用了，
 * 但是每一个 Controller 都写这么一段最终封装的逻辑，
 * 这些都是很重复的工作，所以还要继续想办法进一步处理统一返回结构
 *
 *  Spring 中提供了一个类 ResponseBodyAdvice
 *
 *  ResponseBodyAdvice 是对 Controller 返回的内容在 HttpMessageConverter 进行类型转换之前拦截，
 *  进行相应的处理操作后，再将结果返回给客户端。那这样就可以把统一包装的工作放到这个类里面。
 */
// 如果引入了swagger或knife4j的文档生成组件，这里需要仅扫描自己项目的包，否则文档无法正常生成
@RestControllerAdvice(basePackages = "com.example.demo")
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    /**
     * supports：判断是否要交给 beforeBodyWrite 方法执行，ture：需要；false：不需要
     * @param returnType
     * @param converterType
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 如果不需要进行封装的，可以添加一些校验手段，比如添加标记排除的注解
        return true;
    }

    /**
     * beforeBodyWrite：对 response 进行具体的处理
     *
     * [问题]处理 cannot be cast to java.lang.String 问题
     *
     * [问题定位]如果直接使用 ResponseBodyAdvice，对于一般的类型都没有问题，
     * 当处理字符串类型时，会抛出 xxx.包装类 cannot be cast to java.lang.String 的类型转换的异常
     *
     * 【分析】在 ResponseBodyAdvice 实现类中 debug 发现，
     * 只有 String 类型的 selectedConverterType 参数值是 org.springframework.http.converter.StringHttpMessageConverter，
     * 而其他数据类型的值是 org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
     *
     * 现在问题已经较为清晰了，因为我们需要返回一个 Result 对象
     * 所以使用 MappingJackson2HttpMessageConverter 是可以正常转换的
     * 而使用 StringHttpMessageConverter 字符串转换器会导致类型转换失败
     *
     * 【处理方式】
     *
     * 【1】在 beforeBodyWrite 方法处进行判断，
     * 如果返回值是 String 类型就对 Result 对象手动进行转换成 JSON 字符串，另外方便前端使用，
     * 最好在 @RequestMapping 中指定 ContentType
     * ------------------------------------
     *   if (body instanceof String) {
     *                try {
     *                    return this.objectMapper.writeValueAsString(Result.success(body));
     *                } catch (JsonProcessingException e) {
     *                    throw new RuntimeException(e);
     *                }
     *            }
     * ----------------------------------------------
     * 【2】修改 HttpMessageConverter 实例集合中 MappingJackson2HttpMessageConverter 的顺序。
     * 因为发生上述问题的根源所在是集合中 StringHttpMessageConverter 的顺序先于 MappingJackson2HttpMessageConverter 的，
     * 调整顺序后即可从根源上解决这个问题
     *  ----------------------------------
     *  @Configuration
     *    public class WebMvcConfiguration implements WebMvcConfigurer {
     *        //交换MappingJackson2HttpMessageConverter与第一位元素
     *        //让返回值类型为String的接口能正常返回包装结果
     *        @Override
     *        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
     *            for (int i = 0; i < converters.size(); i++) {
     *                if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
     *                    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converters.get(i);
     *                    converters.set(i, converters.get(0));
     *                    converters.set(0, mappingJackson2HttpMessageConverter);
     *                    break;
     *                }
     *            }
     *        }
     *    }
     *   ----------------------------------
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 提供一定的灵活度，如果body已经被包装了，就不进行包装
        if (body instanceof Result) {
            return body;
        }
        return Result.success(body);
    }
}