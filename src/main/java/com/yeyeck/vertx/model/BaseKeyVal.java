
/**
 * Project Name: 
 * File Name: BaseKeyVal.java
 * @date 2020-11-314:53:56
 * Copyright (c) 2020 .com All Rights Reserved.
 */

package com.yeyeck.vertx.model;

import java.io.Serializable;

//import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * TODO <br/>
 * @date 2020-11-3 14:53:56
 * @author jiangpeiquan
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BaseKeyVal<K, V> implements Serializable {

	/**
	 * serialVersionUID:
	 */
	private static final long serialVersionUID = 1L;

//	@ApiModelProperty("元素名")
	private K key;

//	@ApiModelProperty("元素值")
	private V value;
}