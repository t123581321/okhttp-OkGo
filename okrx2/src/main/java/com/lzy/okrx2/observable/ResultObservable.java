/*
 * Copyright (C) 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzy.okrx2.observable;

import com.lzy.okgo.model.HttpResponse;
import com.lzy.okgo.model.Result;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;

public class ResultObservable<T> extends Observable<Result<T>> {
    private final Observable<HttpResponse<T>> upstream;

    public ResultObservable(Observable<HttpResponse<T>> upstream) {
        this.upstream = upstream;
    }

    @Override
    protected void subscribeActual(Observer<? super Result<T>> observer) {
        upstream.subscribe(new ResultObserver<T>(observer));
    }

    private static class ResultObserver<R> implements Observer<HttpResponse<R>> {
        private final Observer<? super Result<R>> observer;

        ResultObserver(Observer<? super Result<R>> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            observer.onSubscribe(disposable);
        }

        @Override
        public void onNext(HttpResponse<R> response) {
            observer.onNext(Result.response(response));
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                observer.onNext(Result.<R>error(throwable));
            } catch (Throwable t) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
                return;
            }
            observer.onComplete();
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }
    }
}
