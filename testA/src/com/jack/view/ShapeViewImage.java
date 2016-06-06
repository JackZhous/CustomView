package com.jack.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.example.testa.R;

public class ShapeViewImage extends ImageView{

	private static final String TAG = "ShapeViewImage";
	private Context context;
	
	private int border_size = 0;						//边狂厚度
	private int in_border_color = 0;						//内边框颜色
	private int out_border_color = 0;					//外边框颜色
	private int defColor = 0xFFFFFF;					//默认颜色
	
	private int width = 0;
	private int height = 0;
	private String shape_type;							//形状类型
	
	private float star_x[] = new float[10];
	private float star_y[] = new float[10];
	
	public ShapeViewImage(Context context) {
		super(context);
		this.context = context;
	}
	
	public ShapeViewImage(Context context, AttributeSet attrs){
		super(context, attrs);
		this.context = context;
		setAttributes(attrs);
	}
	
	
	public ShapeViewImage(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		this.context = context;
		setAttributes(attrs);
	}

	/*****
	 * 获取自定义属性
	 * @param attrs
	 */
	private void setAttributes(AttributeSet attrs){
		if(null == attrs){
			return;
		}
		
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.shapeimageview);				//获取属性集合
		border_size = array.getDimensionPixelSize(R.styleable.shapeimageview_border_size, 0);				//获取边框厚度
		in_border_color = array.getColor(R.styleable.shapeimageview_in_border_color, defColor);				//获取内边框颜色
		out_border_color = array.getColor(R.styleable.shapeimageview_out_border_color, defColor);
		shape_type = array.getString(R.styleable.shapeimageview_shape_type);
		
		array.recycle();						//回收TypeArray
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Drawable draw = getDrawable();								//返回这个ImageView控件里面的图片  如果没有则是null
		if(null == draw){
			return;
		}
		if(getWidth() == 0 || getHeight() == 0){
			return;
		}
		
		this.measure(0, 0);											//当然，measure完后，并不会实际改变View的尺寸，需要调用View.layout方法去进行布局。按示例调用layout函数后，View的大小将会变成你想要设置成的大小。
		if(draw.getClass() == NinePatchDrawable.class){				//如果是.9.png的图，不处理
			return;
		}
		
		//将图片转换为位图
		Bitmap bitmap = ((BitmapDrawable) draw).getBitmap();
		Bitmap cpbitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);				//true 表示新生成的位图像素有可能被修改
		
		//我觉得是得到组件view的高宽
		width = getWidth();
		height = getHeight();
		Log.i(TAG, "view width -- " + width +" , height -- " + height);
		Log.i(TAG,"shape_type " + shape_type);
		int radius = ((width < height) ? width : height ) / 2;
		//如果是圆的情况
		if("round".equals(shape_type)){
			
			//内外圆边框都存在的情况
			if(in_border_color != defColor && out_border_color != defColor){
				radius = radius - 2 * border_size;
				drawCircleBorder(canvas, radius + border_size / 2,	in_border_color);							//画内圆边框
				drawCircleBorder(canvas, radius + border_size + border_size / 2, out_border_color);				//画外圆边框
			//存在内圆边框
			}else if(in_border_color != defColor && out_border_color == defColor){
				radius = radius - border_size;
				drawCircleBorder(canvas, radius + border_size / 2,	in_border_color);
			}else if(in_border_color == defColor && out_border_color != defColor){
				radius = radius - border_size;
				drawCircleBorder(canvas, radius + border_size / 2,	out_border_color);
			}
			
		}
		Bitmap shapeView = drawShapeBitmap(cpbitmap,radius);
		canvas.drawBitmap(shapeView, width / 2 - radius, height / 2 - radius, null);
	}
	
	/******
	 * 画圆的边框
	 * @param canvas
	 * @param radius  
	 * @param color
	 * 
	 */
	private void drawCircleBorder(Canvas canvas, int radius, int color){
		Paint p = new Paint();
		
		p.setAntiAlias(true);					//抗锯齿
		p.setFilterBitmap(true);				//滤波  不懂啥  反正也是有助于抗锯齿的
		p.setDither(true);						//设置防抖动
		p.setColor(color);
		p.setStyle(Paint.Style.STROKE);			//设置画笔形状  空心
		p.setStrokeWidth(border_size);			//设置画笔宽度
		
		
		canvas.drawCircle(width/2, height/2, radius, p);			//画圆
	}
	
	/**
	 * 根据传入的位图画出不同的图形
	 * @param bitmap 
	 * @param radius   ---  我们设置组件view半径尺寸,去除边框填充内容的半径尺寸
	 * 该方法里面涉及的诸多尺寸是指bitmap位图的尺寸
	 */
	private Bitmap drawShapeBitmap(Bitmap bmp, int radius){
		Bitmap squareBitmap = null;									//对位图切割后的正方形位图
		Bitmap secBitmap = null;									//适应到组件view里面的位图
		int square_length = 0;										//正方形长度
		int location_x = 0;
		int location_y = 0;
		
		int diameter = radius + radius;									//直径
		int width = 0;
		int height = 0;
		
		width = bmp.getWidth();
		height = bmp.getHeight();
		Log.i(TAG, "picture width" + width + " height " + height);
		square_length = (width < height) ? width : height;
		Log.i(TAG, "切割到正方形目的尺寸size: " + square_length);
		//高宽不等进行切割
		if(width != height){
			
			if(square_length == width){						//高大于宽
				location_x = 0;
				location_y = (height - square_length ) / 2;
			}else{											//宽大于高
				location_x = (width - square_length) / 2;
				location_y = 0;
			}
			Log.i(TAG	, "切割目标图片的起点坐标位置");
			Log.i(TAG, "location_x -- " + location_x + "  location_y " + location_y);
			squareBitmap = Bitmap.createBitmap(bmp, location_x, location_y, square_length, square_length);
		//高宽相等不用切割
		}else{
			squareBitmap = bmp;
		}
		
		//位图需要适应到组件里面去，进行缩放
		Log.i(TAG, "创建切割后正方形目的尺寸size, width " + squareBitmap.getWidth() + " height " + squareBitmap.getHeight());
		if(squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter){
			secBitmap = Bitmap.createScaledBitmap(squareBitmap, diameter, diameter, true);				//像素有可能发生变化
		}else{
			secBitmap = squareBitmap;
		}
		Log.i(TAG, "secBitmap 自适应到组件里面的尺寸,width " + secBitmap.getWidth() + " height "+ secBitmap.getHeight());
		//创建一个和目标位图的大小空白画布
		Bitmap outputBitmap = Bitmap.createBitmap(secBitmap.getWidth(), secBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(outputBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);							//初始化画布的颜色，几个值分别是： 透明度  红 绿 蓝
		
		//图形类型为五角星
		if("star".equals(shape_type)){
			Log.i(TAG, "我们要画一个五角星");
			Path path = new Path();
			
			float radians = degreeToRadian(36);					//五角星外面三角形的角度 转换为幅度
			float half_radians = radians / 2;
			float in_radius = Math.abs((float)((Math.sin(half_radians) * radius ) / Math.cos(radians)));					//计算内五边形的半径，是包容內五边形的圆，不是内切五边形的圆
			Log.i(TAG, "内切圆半径是: " + in_radius  + " 外圆半径 " + radius);
			/**
			 * 确定五角星的轨迹  标记路径上的每个点并连接起来  从五角星头上那个点开始，从左到右  从上到下连接五角星轨迹
			 */
			star_x[0] = Math.abs((float)(Math.cos(half_radians) * radius));
			star_y[0] = 0;
			
			star_x[1] = (float)(star_x[0] + Math.abs(in_radius * Math.sin(radians)));
			star_y[1] = (float)(radius - Math.abs(Math.cos(radians) * in_radius));
			
			star_x[2] = star_x[0] + star_x[0];
			star_y[2] = star_y[1];
			
			star_x[3] = (float)(star_x[0] + Math.abs(Math.sin(half_radians) * star_x[1]));
			star_y[3] = Math.abs((float)(Math.cos(half_radians) * star_x[1]));
				
			star_x[4] = (float)(star_x[0] + Math.abs(Math.sin(half_radians) * star_x[2]));
			star_y[4] = Math.abs((float)(Math.cos(half_radians) * star_x[2]));
			
			star_x[5] = star_x[0];
			star_y[5] = (float)(radius + in_radius);
			
			star_x[6] = star_x[2] - star_x[4];
			star_y[6] = star_y[4];
			
			star_x[7] = star_x[2] - star_x[3];
			star_y[7] = star_y[3];
			
			star_x[8] = 0;
			star_y[8] = star_y[2];
			
			star_x[9] = star_x[2] - star_x[1];
			star_y[9] = star_y[2];
			
			path.moveTo(star_x[0], star_y[0]);									//	确定五角星顶上的那个点  以此为七点开始作画
			Log.i(TAG,"坐标  0"   + " 是 "+ star_x[0] + " -- " + star_y[0]);
			for(int i = 1; i < star_x.length; i++){
				Log.i(TAG,"坐标 " + i + " 是 "+ star_x[i] + " -- " + star_y[i]);
				path.lineTo(star_x[i], star_y[i]);
			}
			path.close();														//封闭五角星曲线
			canvas.drawPath(path, paint);
		}else if("triangle".equals(shape_type)){								//三角形情况
			Path path = new Path();
			
			path.moveTo(0, 0);
			path.lineTo(radius, diameter);
			path.lineTo(diameter, 0);
			path.close();
			
			canvas.drawPath(path, paint);
		}else if("heart".equals(shape_type)){									//心形图形
			Path path = new Path();
			
			path.moveTo(diameter / 2, diameter / 5);
			path.quadTo(diameter, 0, diameter / 2, diameter);
			path.quadTo(0, 0, diameter / 2, diameter / 5);			//二次贝塞尔曲线  从上一个起点开始  经过参数0和1  终止于参数 2 和3
			
			path.close();		
			canvas.drawPath(path, paint);
		}else{														//默认图形 形
			canvas.drawCircle(secBitmap.getWidth() / 2, secBitmap.getHeight() / 2, secBitmap.getWidth() / 2, paint);
		}
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));								//取两层绘制交集   显示上层
		canvas.drawBitmap(secBitmap, 0, 0, paint);
		
		bmp = null;
		squareBitmap = null;
		secBitmap = null;
		return outputBitmap;
	}
	
	/**
	 * 角度转换幅度
	 * @param degree
	 * @return
	 */
	private float degreeToRadian(int degree){
		return (float)(Math.PI * degree / 180);
	}
	
}
