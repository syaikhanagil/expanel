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
import android.os.*;
import android.support.annotation.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import net.syaikhanagil.expanel.*;
/**
 * Created By Syaikhan Agil on 29/05/2019
 **/
public class ExPanelHeader extends FrameLayout
 {

    boolean toggleOnClick = true;
    @Nullable
    View mHeaderIndicator;
    @Nullable
    ExPanel expanel;
    @Nullable
    Animator mIndicatorAnimator;
    int mHeaderIndicatorId = 0;
    int mExpanelId = 0;
    private int mHeaderRotationExpanded = 270;
    private int mHeaderRotationCollapsed = 90;
    private boolean mExpanelInit = false;

    @Nullable
    View headerIndicator;
    @Nullable
    Animator indicatorAnimator;

    public ExPanelHeader(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ExPanelHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExPanelHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExPanelHeader);
            if (a != null) {
                setHeaderRotationExpanded(a.getInt(R.styleable.ExPanelHeader_expanel_IndicatorRotationExpand, mHeaderRotationExpanded));
                setHeaderRotationCollapsed(a.getInt(R.styleable.ExPanelHeader_expanel_IndicatorRotationCollapsed, mHeaderRotationCollapsed));
                setHeaderIndicatorId(a.getResourceId(R.styleable.ExPanelHeader_expanel_Indicator, mHeaderIndicatorId));
                setExPanelId(a.getResourceId(R.styleable.ExPanelHeader_expanel_layout, mExpanelId));
                setToggleOnClick(a.getBoolean(R.styleable.ExPanelHeader_expanel_toggleOnClick, toggleOnClick));
                a.recycle();
            }
        }
    }

    public void setHeaderRotationExpanded(int mHeaderRotationExpanded) {
        this.mHeaderRotationExpanded = mHeaderRotationExpanded;
    }

    public void setHeaderRotationCollapsed(int mHeaderRotationCollapsed) {
        this.mHeaderRotationCollapsed = mHeaderRotationCollapsed;
    }

    public void setHeaderIndicatorId(int mHeaderIndicatorId) {
        this.mHeaderIndicatorId = mHeaderIndicatorId;
        if (mHeaderIndicatorId != 0) {
            mHeaderIndicator = findViewById(mHeaderIndicatorId);
            setExPanelHeaderIndicator(mHeaderIndicator);
        }
    }

    public void setExPanelHeaderIndicator(@Nullable View mHeaderIndicator) {
        this.mHeaderIndicator = mHeaderIndicator;

        //if not, the view will clip when rotate
        if (mHeaderIndicator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mHeaderIndicator.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        setup();
    }

    public void setExPanel(@Nullable ExPanel expanel) {
        this.expanel = expanel;
        setup();
    }

    public void setExPanelId(int mExpanelId) {
        this.mExpanelId = mExpanelId;

        if (mExpanelId != 0) {
            final ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                final View view = ((ViewGroup) parent).findViewById(mExpanelId);
                if (view instanceof ExPanel) {
                    setExPanel(((ExPanel) view));
                }
            }
        }
    }

	public boolean isToggleOnClick() {
        return toggleOnClick;
    }

    public void setToggleOnClick(boolean toggleOnClick) {
        this.toggleOnClick = toggleOnClick;
    }
	

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setHeaderIndicatorId(this.mHeaderIndicatorId); //setup or update
        setExPanelId(this.mExpanelId); //setup or update
        setup();
    }

    private void setup() {
        if (expanel != null && !mExpanelInit) {
            expanel.setIndicatorListener(new ExPanel.IndicatorListener() {
					@Override
					public void onStartedExpand(ExPanel expanel, boolean willExpand) {
						onExpanelModify(willExpand);
					}
				});

            setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						if (toggleOnClick) {
							expanel.toggle(true);
						}
					}
				});

            initialiseView(expanel.isExpanded());
            mExpanelInit = true;
        }
    }

    protected void initialiseView(boolean isExpanded) {
        if (mHeaderIndicator != null) {
            mHeaderIndicator.setRotation(isExpanded ? mHeaderRotationExpanded : mHeaderRotationCollapsed);
        }
    }

    protected void onExpanelModify(boolean willExpand) {
        setSelected(willExpand);
        if (mHeaderIndicator != null) {
            if (mIndicatorAnimator != null) {
                mIndicatorAnimator.cancel();
            }
            if (willExpand) {
                mIndicatorAnimator = ObjectAnimator.ofFloat(mHeaderIndicator, View.ROTATION, mHeaderRotationExpanded);
            } else {
                mIndicatorAnimator = ObjectAnimator.ofFloat(mHeaderIndicator, View.ROTATION, mHeaderRotationCollapsed);
            }

            mIndicatorAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mIndicatorAnimator = null;
					}
				});

            if (mIndicatorAnimator != null) {
                mIndicatorAnimator.start();
            }
        }
    }

    public boolean isExpanded() {
        return expanel != null && expanel.isExpanded();
    }

    public void setExPanelListener(ExPanel.ExListener listener) {
        if (expanel != null) {
            expanel.setExPanelListener(listener);
        }
    }

    public void removeListener(ExPanel.ExListener listener) {
        if (expanel != null) {
            expanel.removeListener(listener);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle savedInstance = new Bundle();
        savedInstance.putParcelable("super", super.onSaveInstanceState());

        savedInstance.putInt("mHeaderIndicatorId", mHeaderIndicatorId);
        savedInstance.putInt("mExpanelId", mExpanelId);
        savedInstance.putBoolean("toggleOnClick", toggleOnClick);
        savedInstance.putInt("mHeaderRotationExpanded", mHeaderRotationExpanded);
        savedInstance.putInt("mHeaderRotationCollapsed", mHeaderRotationCollapsed);

        return savedInstance;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle savedInstance = (Bundle) state;

            mHeaderIndicatorId = savedInstance.getInt("mHeaderIndicatorId");
            mExpanelId = savedInstance.getInt("mExpanelId");
            setToggleOnClick(savedInstance.getBoolean("toggleOnClick"));
            setHeaderRotationExpanded(savedInstance.getInt("mHeaderRotationExpanded"));
            setHeaderRotationCollapsed(savedInstance.getInt("mHeaderRotationCollapsed"));
            //setup(); will wait to onAttachToWindow

            mExpanelInit = false;

            super.onRestoreInstanceState(savedInstance.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Nullable
    public View getHeaderIndicator() {
        return mHeaderIndicator;
    }
}

