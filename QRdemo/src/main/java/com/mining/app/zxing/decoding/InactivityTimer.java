/*
 * Copyright (C) 2010 ZXing authors
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

package com.mining.app.zxing.decoding;

import android.app.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Finishes an activity after a period of inactivity.
 *
 * 如果一段时间之后没有接收到信息的话就关闭activity
 */
public final class InactivityTimer {
//设置过多长时间之后关闭activity
  private static final int INACTIVITY_DELAY_SECONDS = 5 * 60;
  /**
   *
   *
   * ScheduledExecutorService是从Java SE5的java.util.concurrent里，做为并发工具类被引进的，这是最理想的定时任务实现方式。
   * 相比于上两个方法，它有以下好处：
   * 1>相比于Timer的单线程，它是通过线程池的方式来执行任务的
   * 2>可以很灵活的去设定第一次执行任务delay时间
   * 3>提供了良好的约定，以便设定执行的时间间隔
   *
   *
   *
   * @author GT
   *
   */
  private final ScheduledExecutorService inactivityTimer =
      Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
  private final Activity activity;
  //ScheduledFuture它用于表示ScheduledExecutorService中提交了任务的返回结果。
  private ScheduledFuture<?> inactivityFuture = null;
//构造方法
  public InactivityTimer(Activity activity) {
    this.activity = activity;
    onActivity();
  }

  public void onActivity() {
    cancel();
    //过多长的时间执行FinishListener任务
    inactivityFuture = inactivityTimer.schedule(new FinishListener(activity),
                                                INACTIVITY_DELAY_SECONDS,
                                                TimeUnit.SECONDS);
  }

  private void cancel() {
    if (inactivityFuture != null) {
      //在需要关闭该定时任务的时候调用：这里的true表示如果定时任务在执行，立即中止，false则等待任务结束后再停止。
      inactivityFuture.cancel(true);
      inactivityFuture = null;
    }
  }
//关闭定时器
  public void shutdown() {
    cancel();
    inactivityTimer.shutdown();
  }

  private static final class DaemonThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable);
      //设置该线程为守护线程
      thread.setDaemon(true);
      return thread;
    }
  }

}
