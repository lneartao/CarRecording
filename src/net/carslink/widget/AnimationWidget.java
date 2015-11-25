package net.carslink.widget;
/**
 * 类说明：控件动画
 * author:Giam
 * 时间：204/10/12
 */

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationWidget{

	@SuppressWarnings("unused")
	private Context context;
	private AnimationSet animationSet;
	private ScaleAnimation scaleAnimation = null;
	private RotateAnimation rotateAnimation = null;
	private AlphaAnimation alphaAnimation = null;
	private TranslateAnimation translateAnimation;
	
	public AnimationWidget(Context context) {
		super();
		
		this.context = context;
		animationSet = new AnimationSet(true);
		animationSet.setInterpolator(new LinearInterpolator());
	}
	
	/**
	 * 旋转动画
	 * @param start 开始角度
	 * @param end 结束角度
	 * @param x x坐标
	 * @param y y坐标
	 * @param runTime 动画时间
	 * @param repeatCount 循环次数
	 * @return rotateAnimation
	 */
	public RotateAnimation mRotateAnimation(float start, float end, float x, float y, int runTime, int repeatCount,boolean stay){
		rotateAnimation = new RotateAnimation(
											start, end,
											Animation.RELATIVE_TO_SELF, x, 
											Animation.RELATIVE_TO_SELF, y
											);
		
		rotateAnimation.setDuration(runTime);
		rotateAnimation.setRepeatCount(repeatCount);
		rotateAnimation.setFillAfter(stay);
		
		return rotateAnimation;
	}
	
	/**
	 * 缩放动画
	 * @param startW x轴开始长度
	 * @param endW x轴结束长度
	 * @param startH y轴开始长度
	 * @param endH y轴结束长度
	 * @param x x坐标
	 * @param y y坐标
	 * @param runTime 动画时间
	 * @param repeatCount 循环次数
	 * @param stay 是否停留在最后的动画
	 * @return scaleAnimation
	 */
	public ScaleAnimation mScaleAnimation(float startW, float endW, float startH, float endH,float x, float y,int runTime, int repeatCount,boolean stay){
		scaleAnimation = new ScaleAnimation(
											startW, endW, 
											startH, endH, 
											Animation.RELATIVE_TO_SELF, x, 
											Animation.RELATIVE_TO_SELF, y
											);
		
		scaleAnimation.setDuration(runTime);
		scaleAnimation.setRepeatCount(repeatCount);

		scaleAnimation.setFillAfter(stay);
		
		return scaleAnimation;
	}
	
	/**
	 * 透明动画
	 * @param start 开始透明度
	 * @param end 结束透明度
	 * @param runTime 动画时间
	 * @param repeatCount 循环次数
	 * @return mAlphaAnimation
	 */
	public AlphaAnimation mAlphaAnimation(float start, float end, int runTime, int repeatCount,boolean stay){
		alphaAnimation = new AlphaAnimation(start, end);
		
		alphaAnimation.setDuration(runTime);
		alphaAnimation.setRepeatCount(repeatCount);
		alphaAnimation.setFillAfter(stay);
		return alphaAnimation;
	}
	
	/**
	 * 移动动画
	 * @param fromXValue 位置变化的起始点X坐标
	 * @param toXValue 位置变化的结束点X坐标
	 * @param fromYValue 位置变化的起始点Y坐标
	 * @param toYValue 位置变化的结束点Y坐标
	 * @param runTime 动画时间
	 * @param repeatCount 循环次数
	 * @return
	 */
	public TranslateAnimation mTranslateAnimation(
												float fromXValue, 
												float toXValue, 
												float fromYValue, 
												float toYValue, 
												int runTime, int repeatCount,
												boolean stay){
		
		translateAnimation = new TranslateAnimation(
											Animation.RELATIVE_TO_SELF, fromXValue, 
											Animation.RELATIVE_TO_SELF, toXValue, 
											Animation.RELATIVE_TO_SELF, fromYValue, 
											Animation.RELATIVE_TO_SELF, toYValue
											);
		translateAnimation.setDuration(runTime);
		translateAnimation.setRepeatCount(repeatCount);
		translateAnimation.setFillAfter(stay);
		
		return translateAnimation;		
	}
	
	public AnimationSet getAnimationSet(){
		if(rotateAnimation != null)
			animationSet.addAnimation(rotateAnimation);
		
		if(scaleAnimation != null)
			animationSet.addAnimation(scaleAnimation);
		
		if(alphaAnimation != null)
			animationSet.addAnimation(alphaAnimation);
		
		if(translateAnimation != null)
			animationSet.addAnimation(translateAnimation);
		
		System.gc();//释放内存
		
		return animationSet;	
	}


	/**
	 * 
	 * @return 缩放动画
	 */
	public ScaleAnimation getScaleAnimation() {
		return scaleAnimation;
	}

	/**
	 * 
	 * @return 旋转动画
	 */
	public RotateAnimation getRotateAnimation() {
		return rotateAnimation;
	}

	/**
	 * 
	 * @return 渐变透明
	 */
	public AlphaAnimation getAlphaAnimation() {
		return alphaAnimation;
	}
	
	
	/**
	 * 
	 * @return 位移动画
	 */
	public TranslateAnimation getTranslateAnimation() {
		return translateAnimation;
	}
}
