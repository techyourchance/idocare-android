package il.co.idocare.screens.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import il.co.idocare.R;


public  final class IdcViewUtils {

    private IdcViewUtils() {}


    public static void showProgressOverlay(View progressOverlayView) {
        if (progressOverlayView.getId() != R.id.element_progress_overlay) {
            throw new IllegalStateException("should be used for progress overlay only");
        }
        animateViewVisibilityChange(progressOverlayView, View.VISIBLE, 0.6f, 200);
    }

    public static void hideProgressOverlay(View progressOverlayView) {
        if (progressOverlayView.getId() != R.id.element_progress_overlay) {
            throw new IllegalStateException("should be used for progress overlay only");
        }
        animateViewVisibilityChange(progressOverlayView, View.GONE, 0, 200);
    }

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateViewVisibilityChange(final View view,
                                                   final int toVisibility,
                                                   float toAlpha,
                                                   int duration) {
        if (view.getVisibility() == toVisibility) {
            return;
        }

        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

}
