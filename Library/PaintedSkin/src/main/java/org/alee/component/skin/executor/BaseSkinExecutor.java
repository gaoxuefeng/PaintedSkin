package org.alee.component.skin.executor;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import org.alee.component.skin.exception.ApplySkinException;
import org.alee.component.skin.pack.IThemeSkinPack;
import org.alee.component.skin.pack.ResourcesType;
import org.alee.component.skin.service.ThemeSkinService;

/**********************************************************
 *
 * @author: MY.Liu
 * @date: 2021/2/13
 * @description: xxxx
 *
 *********************************************************/
public abstract class BaseSkinExecutor<T extends View> implements ISkinExecutor {
    /**
     * {@link SkinElement}
     */
    private final SkinElement mElement;

    public BaseSkinExecutor(@NonNull SkinElement element) {
        mElement = element;
    }

    /**
     * 执行皮肤替换工作
     *
     * @param view 需要替换皮肤的View
     * @throws ApplySkinException 换肤时出现的异常
     */
    @Override
    public final void execute(@NonNull View view) throws ApplySkinException {
        if (null == view) {
            return;
        }
        try {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                applyThemeSkin((T) view, mElement);
            } else {
                view.post(new MainRunnable<>((T) view, mElement, this));
            }
        } catch (Throwable e) {
            throw new ApplySkinException(view, mElement, e);
        }
    }

    @Override
    public final String getSkinAttrName() {
        return mElement.getAttrName();
    }

    private void applyThemeSkin(@NonNull T view, @NonNull SkinElement element) {
        IThemeSkinPack skinPack = ThemeSkinService.getInstance().getCurrentThemeSkinPack();
        if (skinPack == null) {
            return;
        }
        switch (element.getResourcesType()) {
            case ResourcesType.COLOR:
                ColorStateList colorStateList = skinPack.getColorStateList(element.getResourcesId());
                if (null != colorStateList) {
                    applyColor(view, colorStateList, element.getAttrName());
                    break;
                }
                applyColor(view, skinPack.getColor(element.getResourcesId()), element.getAttrName());
                break;
            case ResourcesType.DRAWABLE:
                Drawable drawable = skinPack.getDrawable(element.getResourcesId());
                if (drawable != null) {
                    applyDrawable(view, drawable, element.getAttrName());

                }
                break;
            case ResourcesType.MIPMAP:
                Drawable mipmap = skinPack.getMipmap(element.getResourcesId());
                if (mipmap != null) {
                    applyDrawable(view, mipmap, element.getAttrName());

                }
                break;
            default:
                break;
        }
    }

    /**
     * 适用 颜色
     *
     * @param view           view
     * @param colorStateList 颜色状态集合
     * @param attrName       属性名称 例 textColor
     */
    protected abstract void applyColor(@NonNull T view, @NonNull ColorStateList colorStateList, @NonNull String attrName);

    /**
     * 适用 颜色
     *
     * @param view     view
     * @param color    色值
     * @param attrName 属性名称 例 textColor
     */
    protected abstract void applyColor(@NonNull T view, @ColorInt int color, @NonNull String attrName);

    /**
     * 适用 图片
     *
     * @param view     view
     * @param drawable 图片
     * @param attrName 属性名称 例 background
     */
    protected abstract void applyDrawable(@NonNull T view, @NonNull Drawable drawable, @NonNull String attrName);

    private static final class MainRunnable<T extends View> implements Runnable {
        private final T mView;
        private final SkinElement mElement;
        private final BaseSkinExecutor<T> mSkinExecutor;

        public MainRunnable(@NonNull T view, @NonNull SkinElement element, @NonNull BaseSkinExecutor<T> skinExecutor) {
            mView = view;
            mElement = element;
            mSkinExecutor = skinExecutor;
        }

        @Override
        public void run() {
            mSkinExecutor.applyThemeSkin(mView, mElement);
        }
    }

}
