// Generated by view binder compiler. Do not edit!
package com.example.myapplication.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.myapplication.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityChoiceLoginBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnOtp;

  @NonNull
  public final Button btnemail;

  private ActivityChoiceLoginBinding(@NonNull LinearLayout rootView, @NonNull Button btnOtp,
      @NonNull Button btnemail) {
    this.rootView = rootView;
    this.btnOtp = btnOtp;
    this.btnemail = btnemail;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityChoiceLoginBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityChoiceLoginBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_choice_login, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityChoiceLoginBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnOtp;
      Button btnOtp = ViewBindings.findChildViewById(rootView, id);
      if (btnOtp == null) {
        break missingId;
      }

      id = R.id.btnemail;
      Button btnemail = ViewBindings.findChildViewById(rootView, id);
      if (btnemail == null) {
        break missingId;
      }

      return new ActivityChoiceLoginBinding((LinearLayout) rootView, btnOtp, btnemail);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
