package com.firstpeople.paintpad.view;

import java.util.ArrayList;

import com.firstpeople.paintpad.interfaces.PaintViewCallBack;
import com.firstpeople.paintpad.interfaces.Shapable;
import com.firstpeople.paintpad.interfaces.ShapesInterface;
import com.firstpeople.paintpad.interfaces.ToolInterface;
import com.firstpeople.paintpad.interfaces.UndoCommand;
import com.firstpeople.paintpad.painttools.Eraser;
import com.firstpeople.paintpad.painttools.PlainPen;
import com.firstpeople.paintpad.painttools.BlurPen;
import com.firstpeople.paintpad.painttools.EmbossPen;

import com.firstpeople.paintpad.shapes.Circle;
import com.firstpeople.paintpad.shapes.Curv;
import com.firstpeople.paintpad.shapes.Line;
import com.firstpeople.paintpad.shapes.Oval;
import com.firstpeople.paintpad.shapes.Rectangle;
import com.firstpeople.paintpad.shapes.Square;
import com.firstpeople.paintpad.utils.BitMapUtils;
import com.firstpeople.paintpad.utils.PaintConstants.ERASER_SIZE;
import com.firstpeople.paintpad.utils.PaintConstants.PEN_SIZE;
import com.firstpeople.paintpad.utils.PaintConstants.PEN_TYPE;
import com.firstpeople.paintpad.utils.PaintConstants.SHAP;

import static com.firstpeople.paintpad.utils.PaintConstants.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View implements UndoCommand {

	boolean canvasIsCreated = false;

	private Canvas mCanvas = null;
	private ToolInterface mCurrentPainter = null;

	/* Bitmap 的配置 */
	private Bitmap mBitmap = null;
	private Bitmap mOrgBitMap = null;

	private int mBitmapWidth = 0;
	private int mBitmapHeight = 0;

	private int mBackGroundColor = DEFAULT.BACKGROUND_COLOR;
	/* paint 的配置 */
	private Paint mBitmapPaint = null;

	private paintPadUndoStack mUndoStack = null;

	private int mPenColor = DEFAULT.PEN_COLOR;;
	private int mPenSize = PEN_SIZE.SIZE_1 ;

	private int mEraserSize = ERASER_SIZE.SIZE_1;

	int mPaintType = PEN_TYPE.PLAIN_PEN;

	private PaintViewCallBack mCallBack = null;
	private int mCurrentShapeType = 0;
	private ShapesInterface mCurrentShape = null;
	private Paint.Style mStyle = Paint.Style.STROKE;

	/* 其他 的配置 */
	private boolean isTouchUp = false;
	private int mStackedSize = UNDO_STACK_SIZE;

	public PaintView(Context context) {
		this(context, null);
	}

	public PaintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PaintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		mCanvas = new Canvas();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mUndoStack = new paintPadUndoStack(this, mStackedSize);
		mPaintType = PEN_TYPE.PLAIN_PEN;
		mCurrentShapeType = SHAP.CURV;
		creatNewPen();
	}

	/**
	 * 回调主函数的onHasDraw函数
	 */
	public void setCallBack(PaintViewCallBack callBack) {
		mCallBack = callBack;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		isTouchUp = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mCanvas.setBitmap(mBitmap);
			creatNewPen();
			mCurrentPainter.touchDown(x, y);
			mUndoStack.clearRedo();
			mCallBack.onTouchDown();
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			mCurrentPainter.touchMove(x, y);
			if (mPaintType == PEN_TYPE.ERASER) {
				mCurrentPainter.draw(mCanvas);
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if (mCurrentPainter.hasDraw()) {
				mUndoStack.push(mCurrentPainter);
				if (mCallBack != null) {
					// 控制undo\redo的现实
					mCallBack.onHasDraw();
				}
			}
			mCurrentPainter.touchUp(x, y);
			// 只有在up的时候才在bitmap上画图，最终显示在view上
			mCurrentPainter.draw(mCanvas);
			invalidate();
			isTouchUp = true;
			break;
		}
		return true;
	}

	/**
	 * 设置具体形状，需要注意的是构造函数中的Painter必须是新鲜出炉的
	 */
	private void setShape() {
		if (mCurrentPainter instanceof Shapable) {
			switch (mCurrentShapeType) {
			case SHAP.CURV:
				mCurrentShape = new Curv((Shapable) mCurrentPainter);
				break;
			case SHAP.LINE:
				mCurrentShape = new Line((Shapable) mCurrentPainter);
				break;
			case SHAP.SQUARE:
				mCurrentShape = new Square((Shapable) mCurrentPainter);
				break;
			case SHAP.RECT:
				mCurrentShape = new Rectangle((Shapable) mCurrentPainter);
				break;
			case SHAP.CIRCLE:
				mCurrentShape = new Circle((Shapable) mCurrentPainter);
				break;
			case SHAP.OVAL:
				mCurrentShape = new Oval((Shapable) mCurrentPainter);
				break;
			default:
				break;
			}
			((Shapable) mCurrentPainter).setShap(mCurrentShape);
		}
	}

	@Override
	public void onDraw(Canvas cv) {
		cv.drawColor(mBackGroundColor);
		// 在外部绘制的方法只有一种，就是先在bitmap上绘制，然后加载到cv
		cv.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		// TouchUp使用BitMap的canvas进行绘制，也就不用再View上绘制了
		if (!isTouchUp) {
			// 平时都只在view的cv上临时绘制
			// earaser不能再cv上绘制，需要直接绘制在bitmap上
			if (mPaintType != PEN_TYPE.ERASER) {
				mCurrentPainter.draw(cv);
			}
		}
	}

	/**
	 * 创建一个新的画笔
	 */
	void creatNewPen() {
		ToolInterface tool = null;
		switch (mPaintType) {
		case PEN_TYPE.PLAIN_PEN:
			tool = new PlainPen(mPenSize, mPenColor, mStyle);
			break;
		case PEN_TYPE.ERASER:
			tool = new Eraser(mEraserSize);
			break;
		case PEN_TYPE.BLUR:
			tool = new BlurPen(mPenSize, mPenColor, mStyle);
			break;
		case PEN_TYPE.EMBOSS:
			tool = new EmbossPen(mPenSize, mPenColor, mStyle);
			break;
		default:
			break;
		}
		mCurrentPainter = tool;
		setShape();
	}

	/**
	 * 当此事件发生时，创建Bitmap并setCanvas
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (!canvasIsCreated) {
			mBitmapWidth = w;
			mBitmapHeight = h;
			creatCanvasBitmap(w, h);
			canvasIsCreated = true;
		}
	}

	/**
	 * 创建画布上的背景图片，同时留有备份
	 */
	public void setForeBitMap(Bitmap bitmap) {
		if (bitmap != null) {
			recycleMBitmap();
			recycleOrgBitmap();
		}
		mBitmap = BitMapUtils.duplicateBitmap(bitmap, getWidth(), getHeight());
		mOrgBitMap = BitMapUtils.duplicateBitmap(mBitmap);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		invalidate();
	}

	/**
	 * 回收原始图片
	 */
	private void recycleOrgBitmap() {
		if (mOrgBitMap != null && !mOrgBitMap.isRecycled()) {
			mOrgBitMap.recycle();
			mOrgBitMap = null;
		}
	}

	/**
	 * 回收图片
	 */
	private void recycleMBitmap() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	/**
	 * 得到当前view的截图
	 */
	public Bitmap getSnapShoot() {
		// 获得当前的view的图片
		setDrawingCacheEnabled(true);
		buildDrawingCache(true);
		Bitmap bitmap = getDrawingCache(true);
		Bitmap bmp = BitMapUtils.duplicateBitmap(bitmap);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		// 将缓存清理掉
		setDrawingCacheEnabled(false);
		return bmp;
	}

	/**
	 * 清空屏幕
	 */
	public void clearAll() {
		recycleMBitmap();
		recycleOrgBitmap();
		mUndoStack.clearAll();
		creatCanvasBitmap(mBitmapWidth, mBitmapHeight);
		invalidate();
	}

	/**
	 * 创建bitMap同时获得其canvas
	 */
	private void creatCanvasBitmap(int w, int h) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);
	}

	/**
	 * 改变当前画笔的类型
	 */
	public void setCurrentPainterType(int type) {
		switch (type) {
		case PEN_TYPE.BLUR:
		case PEN_TYPE.PLAIN_PEN:
		case PEN_TYPE.EMBOSS:
		case PEN_TYPE.ERASER:
			mPaintType = type;
			break;
		default:
			mPaintType = PEN_TYPE.PLAIN_PEN;
			break;
		}
	}

	/**
	 * 改变当前的Shap
	 */
	public void setCurrentShapType(int type) {
		switch (type) {
		case SHAP.CURV:
		case SHAP.LINE:
		case SHAP.RECT:
		case SHAP.CIRCLE:
		case SHAP.OVAL:
		case SHAP.SQUARE:
			mCurrentShapeType = type;
			break;
		default:
			mCurrentShapeType =  SHAP.CURV;
			break;
		}
	}

	/**
	 * 得到当前画笔的类型
	 */
	public int getCurrentPainter() {
		return mPaintType;
	}

	/**
	 * 改变当前画笔的大小
	 */
	public void setPenSize(int size) {
		mPenSize = size;
	}

	/**
	 * 改变当前Eraser的大小
	 */
	public void setEraserSize(int size) {
		mEraserSize = size;
	}

	/**
	 * 得到当前画笔的大小
	 */
	public int getPenSize() {
		return mPenSize;
	}

	/**
	 * 重置状态
	 */
	public void resetState() {
		setCurrentPainterType(PEN_TYPE.PLAIN_PEN);
		setPenColor(DEFAULT.PEN_COLOR);
		setBackGroundColor(DEFAULT.BACKGROUND_COLOR);
		mUndoStack.clearAll();
	}

	/**
	 * 更改背景颜色
	 */
	public void setBackGroundColor(int color) {
		mBackGroundColor = color;
		invalidate();
	}

	/**
	 * 得到背景颜色
	 */
	public int getBackGroundColor() {
		return mBackGroundColor;
	}

	/**
	 * 改变画笔的颜色，在创建新笔的时候就能使用了
	 */
	public void setPenColor(int color) {
		mPenColor = color;
	}

	/**
	 * 得到penColor
	 */
	public int getPenColor() {
		return mPenColor;
	}

	/**
	 * 创建临时背景，没有备份
	 */
	protected void setTempForeBitmap(Bitmap tempForeBitmap) {
		if (null != tempForeBitmap) {
			recycleMBitmap();
			mBitmap = BitMapUtils.duplicateBitmap(tempForeBitmap);
			if (null != mBitmap && null != mCanvas) {
				mCanvas.setBitmap(mBitmap);
				invalidate();
			}
		}
	}

	public void setPenStyle(Style style) {
		mStyle = style;
	}

	public byte[] getBitmapArry() {
		return BitMapUtils.bitampToByteArray(mBitmap);
	}

	@Override
	public void undo() {
		if (null != mUndoStack) {
			mUndoStack.undo();
		}
	}

	@Override
	public void redo() {
		if (null != mUndoStack) {
			mUndoStack.redo();
		}
	}

	@Override
	public boolean canUndo() {
		return mUndoStack.canUndo();
	}

	@Override
	public boolean canRedo() {
		return mUndoStack.canRedo();
	}

	@Override
	public String toString() {
		return "mPaint" + mCurrentPainter + mUndoStack;
	}

	/*
	 * ===================================内部类开始=================================
	 * 内部类，负责undo、redo
	 */
	public class paintPadUndoStack {
		private int m_stackSize = 0;
		private PaintView mPaintView = null;
		private ArrayList<ToolInterface> mUndoStack = new ArrayList<ToolInterface>();
		private ArrayList<ToolInterface> mRedoStack = new ArrayList<ToolInterface>();
		private ArrayList<ToolInterface> mOldActionStack = new ArrayList<ToolInterface>();

		public paintPadUndoStack(PaintView paintView, int stackSize) {
			mPaintView = paintView;
			m_stackSize = stackSize;
		}

		/**
		 * 将painter存入栈中
		 */
		public void push(ToolInterface penTool) {
			if (null != penTool) {
				// 如果undo已经存满
				if (mUndoStack.size() == m_stackSize && m_stackSize > 0) {
					// 得到最远的画笔
					ToolInterface removedTool = mUndoStack.get(0);
					// 所有的笔迹增加
					mOldActionStack.add(removedTool);
					mUndoStack.remove(0);
				}

				mUndoStack.add(penTool);
			}
		}

		/**
		 * 清空所有
		 */
		public void clearAll() {
			mRedoStack.clear();
			mUndoStack.clear();
			mOldActionStack.clear();
		}

		/**
		 * undo
		 */
		public void undo() {
			if (canUndo() && null != mPaintView) {
				ToolInterface removedTool = mUndoStack
						.get(mUndoStack.size() - 1);
				mRedoStack.add(removedTool);
				mUndoStack.remove(mUndoStack.size() - 1);

				if (null != mOrgBitMap) {
					// Set the temporary fore bitmap to canvas.
					// 当载入文件时保存了一份,现在要重新绘制出来
					mPaintView.setTempForeBitmap(mPaintView.mOrgBitMap);
				} else {
					// 如果背景不存在，则重新创建一份背景
					mPaintView.creatCanvasBitmap(mPaintView.mBitmapWidth,
							mPaintView.mBitmapHeight);
				}

				Canvas canvas = mPaintView.mCanvas;

				// First draw the removed tools from undo stack.
				for (ToolInterface paintTool : mOldActionStack) {
					paintTool.draw(canvas);
				}

				for (ToolInterface paintTool : mUndoStack) {
					paintTool.draw(canvas);
				}

				mPaintView.invalidate();
			}
		}

		/**
		 * redo
		 */
		public void redo() {
			if (canRedo() && null != mPaintView) {
				ToolInterface removedTool = mRedoStack
						.get(mRedoStack.size() - 1);
				mUndoStack.add(removedTool);
				mRedoStack.remove(mRedoStack.size() - 1);

				if (null != mOrgBitMap) {
					// Set the temporary fore bitmap to canvas.
					mPaintView.setTempForeBitmap(mPaintView.mOrgBitMap);
				} else {
					// Create a new bitmap and set to canvas.
					mPaintView.creatCanvasBitmap(mPaintView.mBitmapWidth,
							mPaintView.mBitmapHeight);
				}

				Canvas canvas = mPaintView.mCanvas;
				// 所有以前的笔迹都存放在removedStack中
				// First draw the removed tools from undo stack.
				for (ToolInterface sketchPadTool : mOldActionStack) {
					sketchPadTool.draw(canvas);
				}
				// 不管怎样都是从撤销里面绘制，重做只是暂时的存储
				for (ToolInterface sketchPadTool : mUndoStack) {
					sketchPadTool.draw(canvas);
				}

				mPaintView.invalidate();
			}
		}

		public boolean canUndo() {
			return (mUndoStack.size() > 0);
		}

		public boolean canRedo() {
			return (mRedoStack.size() > 0);
		}

		public void clearRedo() {
			mRedoStack.clear();
		}

		@Override
		public String toString() {
			return "canUndo" + canUndo();
		}
	}
	/* ==================================内部类结束 ================================= */

}
