/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.util.Objects;

/**
 * 联系人
 * @author Team Orange
 */
public class Contact implements Cloneable {
	public static final Contact mDefaultValue = new Contact(){
		public Contact setId(Long id){
			throw new IllegalStateException("you cannot chang default value.");
		}

		public Contact setSystemId(Long systemId){
			throw new IllegalStateException("you cannot chang default value.");
		}

		public Contact setSystemLookupKey(String systemLookupKey){
			throw new IllegalStateException("you cannot chang default value.");
		}

		protected boolean isSameClass(Object o) {
			return o.getClass() == getClass().getSuperclass() || o.getClass() == getClass();
		}
	};

	//主键
	private Long mId = null;
	//系统联系人的id
	private Long mSystemId = null;
	//系统联系人提供的用来查找主键的一个字段
	private String mSystemLookupKey = null;

	/**
	 * 构造方法
	 */
	public Contact(){
		super();
	}

	/**
	 * 获取主键
	 * @return
	 */
	public Long getId(){
		return mId;
	}

	/**
	 * 设置主键
	 * @param id
	 * @return
	 */
	public Contact setId(Long id){
		mId = id;
		return this;
	}

	/**
	 * 获取系统联系人提供的id
	 * @return
	 */
	public Long getSystemId(){
		return mSystemId;
	}

	/**
	 * 设置系统联系人id字段
	 * @param systemId
	 * @return
	 */
	public Contact setSystemId(Long systemId){
		mSystemId = systemId;
		return this;
	}

	/**
	 * 获取系统联系人提供查找主键的字段
	 * @return
	 */
	public String getSystemLookupKey(){
		return mSystemLookupKey;
	}

	/**
	 * 设置系统联系人提供的查找主键字段
	 * @param systemLookupKey
	 * @return
	 */
	public Contact setSystemLookupKey(String systemLookupKey){
		mSystemLookupKey = systemLookupKey;
		return this;
	}

	/**
	 * 用来判断提供的类是否与本来相同
	 */
	public boolean equals(Object o) {
		if(o == null)
			return false;
		else if(!isSameClass(o))
			return false;
		else {
			Contact other = (Contact) o;
			return Objects.compare(mId, other.mId)
					&& Objects.compare(mSystemId, other.mSystemId)
					&& Objects.compare(mSystemLookupKey, other.mSystemLookupKey);
		}
	}

	/**
	 * 判断类是否相等
	 * @param o
	 * @return
	 */
	protected boolean isSameClass(Object o) {
		return getClass() == o.getClass() || mDefaultValue.getClass() == o.getClass();
	}

	/**
	 * 克隆联系人
	 */
	public Contact clone() {
		Contact clone = null;
		try {
			clone = (Contact) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone a Contact", e);
		}
		return clone;
	}
}
