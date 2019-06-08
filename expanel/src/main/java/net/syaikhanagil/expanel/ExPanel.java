/**
 * ExPanel - Material Expandable Panel Layout for Android
 *
 * Copyright 2019 Syaikhan Agil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 */

package net.syaikhanagil.expanel;

import android.animation.*;
import android.content.*;
import android.content.res.*;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import java.util.*;
/**
 * Created By Syaikhan Agil on 29/05/2019
 **/
public class ExPanel extends NestedScrollView
 {

    private final List<IndicatorListener> mIndicatorListeners = new ArrayList<>();
    private final List<ExListener> mListeners = new ArrayList<>();
    private boolean isExpand = false;
    private Animator animator;

    public ExPanel(Context context) {
        super(context);
        init(context, null);
    }

    public ExPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        requestDisallowInterceptTouchEvent(true);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExPanel);
            if (a != null) {
                isExpand = a.getBoolean(R.styleable.ExPanel_expanel_isExpand, isExpand);
                a.recycle();
            }
        }
    }

    public void setExPanelListener(ExListener listener) {
        if (listener != null && !mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void removeListener(ExListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void setIndicatorListener(IndicatorListener listener) {
        if (listener != null && !mIndicatorListeners.contains(listener))
            mIndicatorListeners.add(listener);
    }

    public void removeIndicatorListener(IndicatorListener listener) {
        if (listener != null) {
            mIndicatorListeners.remove(listener);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isExpand) {
            setHeight(0f);
        }
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ExPanel can host only one direct child");
        }
        super.addView(child);
        onViewAdded();
    }
    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ExPanel can host only one direct child");
        }
        super.addView(child, index);
        onViewAdded();
    }
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ExPanel can host only one direct child");
        }

        super.addView(child, params);
        onViewAdded();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ExPanel can host only one direct child");
        }

        super.addView(child, index, params);
        onViewAdded();
    }
    private void onViewAdded(){
        if (getChildCount() != 0) {
            final View childView = getChildAt(0);
            childView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						childView.getViewTreeObserver().removeOnPreDrawListener(this);

						//now we have a size
						if (isExpand) {
							expand(false);
						}

						childView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
								@Override
								public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
									if (isExpand && animator == null) {
										final int height = bottom - top;
										post(new Runnable() {
												@Override
												public void run() {
													setHeight(height);

												}
											});
									}
								}
							});

						return true;
					}
				});
        }
    }

    public void collapse(boolean animated) {
        if (!isEnabled() || !isExpand) {
            return;
        }
        pingIndicatorListeners(false);
        if (animated) {
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f * getHeight(), 0f);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						setHeight((Float) valueAnimator.getAnimatedValue());
					}
				});
            valueAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						animator = null;
						pingListeners();
					}
				});
            isExpand = false;
            animator = valueAnimator;
            valueAnimator.start();
        } else {
            setHeight(0f);
            isExpand = false;
            pingListeners();
        }
    }

    private void pingIndicatorListeners(boolean willBeExpanded) {
        for (IndicatorListener indicatorListener : mIndicatorListeners) {
            if (indicatorListener != null) {
                indicatorListener.onStartedExpand(this, willBeExpanded);
            }
        }
    }

    private void pingListeners() {
        for (ExListener listener : mListeners) {
            if (listener != null) {
                listener.onExPanelChanged(this, isExpand);
            }
        }
    }

    public void expand(boolean animated) {
        if (!isEnabled() || isExpand) {
            return;
        }

        pingIndicatorListeners(true);
        if (animated) {
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, getChildAt(0).getHeight());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						setHeight((Float) valueAnimator.getAnimatedValue());
					}
				});
            valueAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						animator = null;
						pingListeners();
					}
				});
            isExpand = true;
            animator = valueAnimator;
            valueAnimator.start();
        } else {
            setHeight(getChildAt(0).getHeight());
            isExpand = true;
            pingListeners();
        }
    }

    private void setHeight(float height) {
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = (int) height;
            setLayoutParams(layoutParams);
        }
    }

    public void toggle(boolean animated) {
        if (isExpand) {
            collapse(animated);
        } else {
            expand(animated);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle savedInstance = new Bundle();
        savedInstance.putParcelable("super", super.onSaveInstanceState());
        savedInstance.putBoolean("isExpand", isExpand);
        return savedInstance;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle){
            final Bundle savedInstance = (Bundle) state;
            boolean isExpand = savedInstance.getBoolean("isExpand");
            if(isExpand){
				expand(false);
            } else {
                collapse(false);
            }
            super.onRestoreInstanceState(savedInstance.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public boolean isExpanded() {
        return isExpand;
    }

    public interface ExListener {
        void onExPanelChanged(ExPanel expanel, boolean isExpand);
    }

    public interface IndicatorListener {
        void onStartedExpand(ExPanel expanel, boolean willExpand);
    }
}

