/**
 *
 */
package org.orange.familylink.util;

/**
 * 和一般对象相关的通用工具类
 * @author Team Orange
 */
public abstract class Objects {
	/**
	 * 判断两对象是否相等，根据o1的<code>equals</code>方法判断。
	 * <p><em>当o1为null时：若o2也为null，返回true；若o2不是null，返回false</em></p>
	 * @param o1 待比较对象1
	 * @param o2 待比较对象2
	 * @return 若o1 == o2，返回true；若o1 != o2，返回false
	 */
	public static boolean compare(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
}
