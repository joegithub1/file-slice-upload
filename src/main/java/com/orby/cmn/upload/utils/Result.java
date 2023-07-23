package com.orby.cmn.upload.utils;


/**
 * 统一返回值
 *
 * @author zhangshuai
 *
 */
public class Result {

	private static Result result = null;
	// 成功状态码
	public static final int SUCCESS_CODE = 0;

	// 请求失败状态码
	public static final int FAIL_CODE = 500;

	// 查无资源状态码
	public static final int NOTF_FOUNT_CODE = 404;

	// 无权访问状态码
	public static final int ACCESS_DINE_CODE = 403;

	public static final int FILE_EXIS = -2 ; //文件已经上传过了 秒传

	public static final int FILE_SHARD_EXIS = -3;//分片已经存在了
	/**
	 * 状态码
	 */
	private int code;

	/**
	 * 提示信息
	 */
	private String msg;

	/**
	 * 数据信息
	 */
	private Object data;

	/**
	 * 请求成功
	 *
	 * @return
	 */
	public static Result ok() {
		Result r = new Result();
		r.setCode(SUCCESS_CODE);
		r.setMsg("请求成功！");
		r.setData(null);
		return r;
	}

	/**
	 * 请求失败
	 *
	 * @return
	 */
	public static Result fail() {
		Result r = new Result();
		r.setCode(FAIL_CODE);
		r.setMsg("请求失败！");
		r.setData(null);
		return r;
	}

	/**
	 * 请求成功，自定义信息
	 *
	 * @param msg
	 * @return
	 */
	public static Result ok(String msg) {
		Result r = new Result();
		r.setCode(SUCCESS_CODE);
		r.setMsg(msg);
		r.setData(null);
		return r;
	}
	public static Result ok(int code,String msg) {
		Result r = new Result();
		r.setCode(code);
		r.setMsg(msg);
		r.setData(null);
		return r;
	}
	/**
	 * 请求失败，自定义信息
	 *
	 * @param msg
	 * @return
	 */
	public static Result fail(String msg) {
		Result r = new Result();
		r.setCode(FAIL_CODE);
		r.setMsg(msg);
		r.setData(null);
		return r;
	}

	/**
	 * 请求成功，自定义信息，自定义数据
	 *
	 * @param msg
	 * @return
	 */
	public static Result ok(String msg, Object data) {
		Result r = new Result();
		r.setCode(SUCCESS_CODE);
		r.setMsg(msg);
		r.setData(data);
		return r;
	}

	/**
	 * 请求失败，自定义信息，自定义数据
	 *
	 * @param msg
	 * @return
	 */
	public static Result fail(String msg, Object data) {
		Result r = new Result();
		r.setCode(FAIL_CODE);
		r.setMsg(msg);
		r.setData(data);
		return r;
	}
	public Result code(Integer code){
		this.setCode(code);
		return this;
	}


	public Result data(Object data){
		this.setData(data);
		return this;
	}

	public Result msg(String msg){
		this.setMsg(msg);
		return this;
	}

	public static synchronized Result getInstance() {
		if (null == result) {
			return result = new Result();
		}
		return result;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Result{" +
				"code=" + code +
				", msg='" + msg + '\'' +
				", data=" + data +
				'}';
	}
}
