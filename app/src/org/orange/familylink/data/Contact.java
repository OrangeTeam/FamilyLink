/**
 *
 */
package org.orange.familylink.data;

import org.orange.familylink.util.Objects;

import android.graphics.Bitmap;

/**
 * 联系人
 * @author Team Orange
 */
public class Contact implements Cloneable {
	public static final Contact mDefaultValue = new Contact(){
		public Contact setId(Long id){
			throw new IllegalStateException("you cannot change default value.");
		}

		public Contact setName(String name){
			throw new IllegalStateException("you cannot change default value.");
		}

		public Contact setPhoneNumber(String phoneNumber){
			throw new IllegalStateException("you cannot change default value.");
		}

		public Contact setPhoto(Bitmap mPhoto){
			throw new IllegalStateException("you cannot change default value.");
		}

		protected boolean isSameClass(Object o) {
			return o.getClass() == getClass().getSuperclass() || o.getClass() == getClass();
		}
	};

	//主键
	private Long mId = null;
	//姓名
	private String mName = null;
	//电话号码
	private String mPhoneNumber = null;
	//位图
	private Bitmap mPhoto = null;

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
	 * 获取姓名
	 * @return
	 */
	public String getName(){
		return mName;
	}

	/**
	 * 设置姓名
	 * @param name
	 * @return
	 */
	public Contact setName(String name){
		mName = name;
		return this;
	}

	/**
	 * 获取电话号码
	 * @return
	 */
	public String getPhoneNumber(){
		return mPhoneNumber;
	}

	/**
	 * 设置电话号码
	 * @param phoneNumber
	 * @return
	 */
	public Contact setPhoneNumber(String phoneNumber){
		mPhoneNumber = phoneNumber;
		return this;
	}

	/**
	 * 获取照片
	 * @return
	 */
	public Bitmap getPhoto(){
		return mPhoto;
	}

	/**
	 * 设置照片
	 * @param photo
	 * @return
	 */
	public Contact setPhoto(Bitmap photo){
		mPhoto = photo;
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
					&& Objects.compare(mName, other.mName)
					&& Objects.compare(mPhoneNumber, other.mPhoneNumber)
					&& Objects.compare(mPhoto, other.mPhoto);
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
			clone.mPhoto = getPhoto();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("can't clone a Contact", e);
		}
		return clone;
	}
}
