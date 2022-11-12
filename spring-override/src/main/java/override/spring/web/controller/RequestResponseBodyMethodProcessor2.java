/*
 *版权所有2002-2018原作者。
 *
 *根据Apache许可证2.0版（“许可证”）获得许可；
 *除非符合许可证，否则您不得使用此文件。
 *您可以在以下地址获得许可证副本：
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *除非适用法律要求或书面同意，软件
 *根据许可证分发的内容按“原样”分发，
 *无任何明示或暗示的担保或条件。
 *有关管理权限和
 *许可下的限制。
 */

package override.spring.web.controller;

import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 *解析用｛@code @RequestBody｝注释的方法参数并处理返回
 *通过读取和写入用｛@code @ResponseBody｝注释的方法的值
 *使用｛@link HttpMessageConverter｝添加到请求或响应的主体。
 *<p>如果对｛@code@RequestBody｝方法参数进行了注释，则它也会被验证
 *与任何
 *｛@linkplain org.springframework.validation.anotation.ValidationAnnotationUtils#determineValidationHints触发验证的注释}。
 * 在验证失败的情况下，
 *｛@link MethodArgumentNotValidException｝被引发并导致HTTP 400
 *如果配置了｛@link DefaultHandlerExceptionResolver｝，则返回响应状态代码。
 *
 * 用于解析 @RequestBody 标注的参数
 * 处理 @ResponseBody 标注方法的返回值
 *
 *@作者Arjen Poutsma
 *@作者Rossen Stoyanchev
 *@作者Juergen Hoeller
 *@自3.1起
 */
public class RequestResponseBodyMethodProcessor2 extends RequestResponseBodyMethodProcessor {

	/**
	 *仅带转换器的基本构造函数。适用于解决
	 *｛@code@RequestBody｝。要处理｛@code@ResponseBody｝，请考虑
	 *提供｛@code ContentNegotiationManager｝。
	 */
	public RequestResponseBodyMethodProcessor2(List<HttpMessageConverter<?>> converters) {
		super(converters);
	}

	/**
	 * Basic constructor with converters and {@code ContentNegotiationManager}.
	 * Suitable for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody} without {@code Request~} or
	 * {@code ResponseBodyAdvice}.
	 */
	public RequestResponseBodyMethodProcessor2(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager manager) {

		super(converters, manager);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} method arguments.
	 * For handling {@code @ResponseBody} consider also providing a
	 * {@code ContentNegotiationManager}.
	 * @since 4.2
	 */
	public RequestResponseBodyMethodProcessor2(List<HttpMessageConverter<?>> converters,
			@Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, null, requestResponseBodyAdvice);
	}

	/**
	 * Complete constructor for resolving {@code @RequestBody} and handling
	 * {@code @ResponseBody}.
	 */
	public RequestResponseBodyMethodProcessor2(List<HttpMessageConverter<?>> converters,
			@Nullable ContentNegotiationManager manager, @Nullable List<Object> requestResponseBodyAdvice) {

		super(converters, manager, requestResponseBodyAdvice);
	}


	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestBody.class);
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		return (AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), ResponseBody.class) ||
				returnType.hasMethodAnnotation(ResponseBody.class));
	}

	/**
	 * 解析 @RequestBoyd 标注参数的方法是 resolveArgument
	 *
	 * Throws MethodArgumentNotValidException if validation fails.
	 * @throws HttpMessageNotReadableException if {@link RequestBody#required()}
	 * is {@code true} and there is no body content or if there is no suitable
	 * converter to read the content with.
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws Exception {

		parameter = parameter.nestedIfOptional();
		//把请求数据封装成标注的DTO对象
		Object arg = readWithMessageConverters(webRequest, parameter, parameter.getNestedGenericParameterType());
		String name = Conventions.getVariableNameForParameter(parameter);

		if (binderFactory != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, arg, name);
			if (arg != null) {
				//执行数据校验
				validateIfApplicable(binder, parameter);
				//如果校验不通过，就抛出MethodArgumentNotValidException异常
				//如果我们不自己捕获，那么最终会由DefaultHandlerExceptionResolver捕获处理
				if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
					throw new MethodArgumentNotValidException(parameter, binder.getBindingResult());
				}
			}
			if (mavContainer != null) {
				mavContainer.addAttribute(BindingResult.MODEL_KEY_PREFIX + name, binder.getBindingResult());
			}
		}

		return adaptArgumentIfNecessary(arg, parameter);
	}

	/**
	 * Validate the binding target if applicable.
	 * <p>The default implementation checks for {@code @javax.validation.Valid},
	 * Spring's {@link org.springframework.validation.annotation.Validated},
	 * and custom annotations whose name starts with "Valid".
	 * @param binder the DataBinder to be used
	 * @param parameter the method parameter descriptor
	 * @since 4.1.5
	 * @see #isBindExceptionRequired
	 */
	@Override
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
		//获取参数上的所有注解
		Annotation[] annotations = parameter.getParameterAnnotations();
		for (Annotation ann : annotations) {
			//如果注解中包含了@Valid、@Validated或者是名字以Valid开头的注解就进行参数校验
			Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
			if (validationHints != null) {
				//实际校验逻辑，最终会调用Hibernate Validator执行真正的校验
				//所以Spring Validation是对Hibernate Validation的二次封装
				binder.validate(validationHints);
				break;
			}
		}
	}


	@Override
	protected <T> Object readWithMessageConverters(NativeWebRequest webRequest, MethodParameter parameter,
			Type paramType) throws IOException, HttpMediaTypeNotSupportedException, HttpMessageNotReadableException {

		HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Assert.state(servletRequest != null, "No HttpServletRequest");
		ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);

		Object arg = readWithMessageConverters(inputMessage, parameter, paramType);
		if (arg == null && checkRequired(parameter)) {
			throw new HttpMessageNotReadableException("Required request body is missing: " +
					parameter.getExecutable().toGenericString(), inputMessage);
		}
		return arg;
	}

	protected boolean checkRequired(MethodParameter parameter) {
		RequestBody requestBody = parameter.getParameterAnnotation(RequestBody.class);
		return (requestBody != null && requestBody.required() && !parameter.isOptional());
	}

	@Override
	public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
			ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
			throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

		mavContainer.setRequestHandled(true);
		ServletServerHttpRequest inputMessage = createInputMessage(webRequest);
		ServletServerHttpResponse outputMessage = createOutputMessage(webRequest);

		// Try even with null return value. ResponseBodyAdvice could get involved.
		writeWithMessageConverters(returnValue, returnType, inputMessage, outputMessage);
	}

}
