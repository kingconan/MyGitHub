package com.scienvo.sample.st.itemanimator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

public class MyItemAnimator extends RecyclerView.ItemAnimator {

    List<RecyclerView.ViewHolder> mViewHolders = new ArrayList<RecyclerView.ViewHolder>();

    @Override
    public void runPendingAnimations() {
        if (!mViewHolders.isEmpty()) {
            int animationDuration = 300;
            AnimatorSet animator;
            View target;
            for (final RecyclerView.ViewHolder viewHolder : mViewHolders) {
                target = viewHolder.itemView;
                target.setPivotX(target.getMeasuredWidth() / 2);
                target.setPivotY(target.getMeasuredHeight() / 2);

                animator = new AnimatorSet();

                animator.playTogether(
                        ObjectAnimator.ofFloat(target, "translationX", -target.getMeasuredWidth(), 0.0f),
                        ObjectAnimator.ofFloat(target, "alpha", target.getAlpha(), 1.0f)
                );

                animator.setTarget(target);
                animator.setDuration(animationDuration);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setStartDelay((animationDuration * viewHolder.getPosition()) / 10);
                animator.addListener(new AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mViewHolders.remove(viewHolder);
                    }

					@Override
					public void onAnimationStart(Animator animation) {
						
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						
					}

					@Override
					public void onAnimationRepeat(Animator animation) {
						
					}
                });
                animator.start();
            }
        }
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder viewHolder) {
        return false;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setAlpha(0.0f);
        return mViewHolders.add(viewHolder);
    }

    @Override
    public boolean animateMove(RecyclerView.ViewHolder viewHolder, int i, int i2, int i3, int i4) {
        return false;
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    public void endAnimations() {
    }

    @Override
    public boolean isRunning() {
        return !mViewHolders.isEmpty();
    }

	@Override
	public boolean animateChange(ViewHolder arg0, ViewHolder arg1, int arg2,
			int arg3, int arg4, int arg5) {
		// TODO Auto-generated method stub
		return false;
	}

}