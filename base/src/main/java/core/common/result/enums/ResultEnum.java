package core.common.result.enums;

import lombok.Data;

//常用结果的枚举
public enum ResultEnum implements IResult {
    SUCCESS(200, "接口调用成功"),
    VALIDATE_FAILED(2002, "参数校验失败"),
    COMMON_FAILED(500, "接口调用失败"),
    FORBIDDEN(510, "没有权限访问资源");

    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    //省略get、set方法和构造方法
}