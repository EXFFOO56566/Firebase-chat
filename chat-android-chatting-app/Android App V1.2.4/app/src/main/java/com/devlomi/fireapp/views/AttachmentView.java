package com.devlomi.fireapp.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.devlomi.fireapp.R;

import io.codetail.animation.SupportAnimator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Devlomi on 29/10/2017.
 */

//this view will show when user click on attachment button
//it will show the 6 items :File,Camera,Gallery,Audio,Location and contact
public class AttachmentView extends RelativeLayout implements View.OnClickListener {

    private CardView cardViewAttachments;
    private AttachmentClickListener listener;

    private LinearLayout attachmentDocument;
    private LinearLayout attachmentCamera;
    private LinearLayout attachmentGallery;
    private LinearLayout attachmentAudio;
    private LinearLayout attachmentLocation;
    private LinearLayout attachmentContact;
    private View attachmentBtn;

    //will indicates if this views is open or closed
    private boolean isOpen = false;

    Context context;

    public AttachmentView(Context context) {
        super(context);
        this.context = context;
        init(context);

    }

    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public AttachmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }


    private void init(Context context) {
        //inflate attachmet items from xml
        View view = View.inflate(context, R.layout.attachments_items, null);
        //set layout params to this view
        final LayoutParams lp = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        view.setLayoutParams(lp);
        //add the inflated view to this custom relative layout
        addView(view);

        //get the views
        cardViewAttachments = view.findViewById(R.id.card_view_attachments);
        attachmentDocument = view.findViewById(R.id.attachment_document);
        attachmentCamera = view.findViewById(R.id.attachment_camera);
        attachmentGallery = view.findViewById(R.id.attachment_gallery);
        attachmentAudio = view.findViewById(R.id.attachment_audio);
        attachmentLocation = view.findViewById(R.id.attachment_location);
        attachmentContact = view.findViewById(R.id.attachment_contact);

        //set on click listeners
        attachmentDocument.setOnClickListener(this);
        attachmentCamera.setOnClickListener(this);
        attachmentGallery.setOnClickListener(this);
        attachmentAudio.setOnClickListener(this);
        attachmentLocation.setOnClickListener(this);
        attachmentContact.setOnClickListener(this);


    }

    //pass on click listener from this to activity
    public void setOnAttachmentClick(AttachmentClickListener listener) {
        this.listener = listener;
    }


    //show or hide the view with reveal animation
    public void reveal(View view) {
        animate(view);
        //get the attachment button to start the animation from its position
        attachmentBtn = view;
    }

    @Override
    public void onClick(View view) {
        if (listener != null)
            //pass the on click event to activity
            listener.OnClick(view.getId());

        //start animating
        if (attachmentBtn != null)
            animate(attachmentBtn);
    }

    //determine if this view is open or closed
    public boolean isShowing() {
        return cardViewAttachments.getVisibility() == VISIBLE;
    }

    private void animate(View view) {

        int w = cardViewAttachments.getWidth();
        int h = cardViewAttachments.getHeight();

        int finalRadius = (int) Math.hypot(w, h);


        //get attachment button x coordinates to start the animation from
        int cx = (int) view.getX();

        int cy = cardViewAttachments.getBottom();


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {


            SupportAnimator animator =
                    io.codetail.animation.ViewAnimationUtils.createCircularReveal(cardViewAttachments, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(1500);

            SupportAnimator animator_reverse = animator.reverse();

            //if it's not open ,open it
            if (!isOpen) {
                cardViewAttachments.setVisibility(View.VISIBLE);
                animator.start();
                isOpen = true;
            } else {
                //if otherwise close it
                animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationEnd() {
                        cardViewAttachments.setVisibility(View.INVISIBLE);
                        isOpen = false;

                    }

                    @Override
                    public void onAnimationCancel() {

                    }

                    @Override
                    public void onAnimationRepeat() {

                    }
                });
                animator_reverse.start();

            }
        } else {
            if (!isOpen) {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(cardViewAttachments, cx, cy, 0, finalRadius);
                cardViewAttachments.setVisibility(View.VISIBLE);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        bounceAnimations();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
                isOpen = true;

            } else {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(cardViewAttachments, cx, cy, finalRadius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cardViewAttachments.setVisibility(View.INVISIBLE);
                        isOpen = false;
                    }
                });
                anim.start();

            }
        }
    }

    //hide the view
    public void hide(View view) {
        isOpen = true;
        animate(view);
    }

    //animating icons in the view when starts revealing
    //it will grow from 0 to 1
    private void bounceAnimations() {
        float startScale = 0.f;
        float endScale = 1.0f;

        ObjectAnimator galleryScaleY = ObjectAnimator.ofFloat(attachmentGallery, "scaleY", startScale, endScale);
        ObjectAnimator galleryScaleX = ObjectAnimator.ofFloat(attachmentGallery, "scaleX", startScale, endScale);

        ObjectAnimator cameraScaleY = ObjectAnimator.ofFloat(attachmentCamera, "scaleY", startScale, endScale);
        ObjectAnimator cameraScaleX = ObjectAnimator.ofFloat(attachmentCamera, "scaleX", startScale, endScale);

        ObjectAnimator documentScaleY = ObjectAnimator.ofFloat(attachmentDocument, "scaleY", startScale, endScale);
        ObjectAnimator documentScaleX = ObjectAnimator.ofFloat(attachmentDocument, "scaleX", startScale, endScale);


        ObjectAnimator audioScaleY = ObjectAnimator.ofFloat(attachmentAudio, "scaleY", startScale, endScale);
        ObjectAnimator audioScaleX = ObjectAnimator.ofFloat(attachmentAudio, "scaleX", startScale, endScale);

        ObjectAnimator locationScaleY = ObjectAnimator.ofFloat(attachmentLocation, "scaleY", startScale, endScale);
        ObjectAnimator locationScaleX = ObjectAnimator.ofFloat(attachmentLocation, "scaleX", startScale, endScale);

        ObjectAnimator contactScaleY = ObjectAnimator.ofFloat(attachmentContact, "scaleY", startScale, endScale);
        ObjectAnimator contactScaleX = ObjectAnimator.ofFloat(attachmentContact, "scaleX", startScale, endScale);

        final AnimatorSet set = new AnimatorSet();
        //animation duration
        set.setDuration(250);
        set.setInterpolator(new AnticipateOvershootInterpolator());
        //start all animation together
        set.playTogether(galleryScaleX, galleryScaleY
                , cameraScaleX, cameraScaleY
                , audioScaleX, audioScaleY
                , documentScaleX, documentScaleY
                , locationScaleX, locationScaleY
                , contactScaleX, contactScaleY);
        set.start();


    }

    public interface AttachmentClickListener {
        void OnClick(int id);
    }

}
