package com.xshengcn.diycode.ui.presenter;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public abstract class BasePresenter<T> {

  private final CompositeDisposable disposables = new CompositeDisposable();
  private Reference<T> reference;

  public void onAttach(T view) {
    reference = new WeakReference<>(view);
  }

  protected void addDisposable(Disposable s) {
    disposables.add(s);
  }

  public CompositeDisposable getDisposables() {
    return disposables;
  }

  public void onDetach() {

    disposables.clear();

    if (reference != null) {
      reference.clear();
      reference = null;
    }
  }

  protected T getView() {
    T t = reference.get();
    if (t == null) {
      throw new NullPointerException("must attach to presenter");
    }
    return reference.get();
  }
}