package com.clubsync.data;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(String error);
}
