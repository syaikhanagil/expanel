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
 **/

package net.syaikhanagil.expanel;

import android.animation.*;
import android.content.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import java.util.*;
/**
 * Created By Syaikhan Agil on 29/05/2019
 **/
public class HorizontalExPanel extends HorizontalScrollView
{

    private final List<IndicatorListener> mIndicatorListeners = new ArrayList<>();
    private final List<Listener> mListeners = new ArrayList<>();
    private boolean isExpand = false;
    private Animator animator;

    public HorizontalExPanel(Context context) {
        super(context);
        init(context, null);
    }

    public HorizontalExPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HorizontalExPanel(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public void addListener(Listener listener) {
        if (listener != null && !mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void addIndicatorListener(IndicatorListener listener) {
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
            setWidth(0f);
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
										final int width = right - left;
										post(new Runnable() {
												@Override
												public void run() {
													setWidth(width);

												}
											});
									}
								}
							});

						return false;
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
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f * getWidth(), 0f);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						setWidth((Float) valueAnimator.getAnimatedValue());
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
            setWidth(0f);
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
        for (Listener listener : mListeners) {
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
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, getChildAt(0).getWidth());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						setWidth((Float) valueAnimator.getAnimatedValue());
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
            setWidth(getChildAt(0).getWidth());
            isExpand = true;
            pingListeners();
        }
    }

    private void setWidth(float width) {
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = (int) width;
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


    public boolean isExpanded() {
        return isExpand;
    }

    public interface Listener {
        void onExPanelChanged(HorizontalExPanel expanel, boolean isExpand);
    }

    public interface IndicatorListener {
        void onStartedExpand(HorizontalExPanel expanel, boolean willExpand);
    }
}

