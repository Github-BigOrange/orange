package core.common.result.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.common.result.enums.IResult;
import core.common.result.enums.ResultEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *   接口返回数据格式
 *
 *
 *   统一返回结构
 *   统一返回值类型无论项目前后端是否分离都是非常必要的，
 *   方便对接接口的开发人员更加清晰地知道这个接口的调用是否成功（不能仅仅简单地看返回值是否为 null 就判断成功与否，因为有些接口的设计就是如此），
 *   使用一个状态码、状态信息就能清楚地了解接口调用情况
 *
 *
 * @author scott
 * @email jeecgos@163.com
 * @date  2019年1月19日
 */
@Data
@ApiModel(value="接口返回对象", description="接口返回对象")
public class Result<T> implements Serializable {

	private static final long serialVersionUID = 1L;


	public Result(boolean success, String message, Integer code, T result, long timestamp, String onlTable) {
		this.success = success;
		this.message = message;
		this.code = code;
		this.result = result;
		this.timestamp = timestamp;
		this.onlTable = onlTable;
	}
	public Result() {

	}


	/**
	 * 成功标志
	 */
	@ApiModelProperty(value = "成功标志")
	private boolean success = true;

	/**
	 * 返回处理消息
	 */
	@ApiModelProperty(value = "返回处理消息")
	private String message = "操作成功！";

	/**
	 * 返回代码
	 */
	@ApiModelProperty(value = "返回代码")
	private Integer code = 0;
	
	/**
	 * 返回数据对象 data
	 */
	@ApiModelProperty(value = "返回数据对象")
	private T result;
	
	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	private long timestamp = System.currentTimeMillis();

	public Result<T> success(String message) {
		this.message = message;
		this.code = ResultEnum.SUCCESS.getCode();
		this.success = true;
		return this;
	}


	public static<T> Result<T> success() {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(ResultEnum.SUCCESS.getCode());
		r.setMessage("成功");
		return r;
	}

	public static<T> Result<T> success(T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(ResultEnum.SUCCESS.getCode());
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> success(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(true);
		r.setCode(ResultEnum.SUCCESS.getCode());
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static<T> Result<T> failed(String msg, T data) {
		Result<T> r = new Result<T>();
		r.setSuccess(false);
		r.setCode(ResultEnum.COMMON_FAILED.getCode());
		r.setMessage(msg);
		r.setResult(data);
		return r;
	}

	public static Result<Object> failed(String msg) {
		return failed(ResultEnum.COMMON_FAILED.getCode(),msg);
	}
	
	public static Result<Object> failed(int code, String msg) {
		Result<Object> r = new Result<Object>();
		r.setCode(code);
		r.setMessage(msg);
		r.setSuccess(false);
		return r;
	}

	public static Result<Object> failed(IResult result) {
		Result<Object> r = new Result<Object>();
		r.setCode(result.getCode());
		r.setMessage(result.getMessage());
		r.setSuccess(false);
		return r;
	}
	public Result<T> failed500(String message) {
		this.message = message;
		this.code = ResultEnum.COMMON_FAILED.getCode();
		this.success = false;
		return this;
	}
	public Result<T> failed500() {
		this.message = ResultEnum.COMMON_FAILED.getMessage();
		this.code = ResultEnum.COMMON_FAILED.getCode();
		this.success = false;
		return this;
	}
	/**
	 * 无权限访问返回结果
	 */
	public static Result<Object> noAuth(String msg) {
		return failed(ResultEnum.FORBIDDEN.getCode(), msg);
	}



	@JsonIgnore
	private String onlTable;

}