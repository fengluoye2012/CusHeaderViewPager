# CusHeaderViewPager 用来实现可滑动头布局的ViewPager

问题：
  在实际的项目中会遇到很多类似个人主页的情况，一个页面中包含多个Fragmeng同时还存在一个Header,
  类似RecyclerView可以滚动Header

解决方案：
 - 自定义HeaderView实现LinearLayout,处理事件事件分发和滑动；


### Measure的主要概念和流程
    - MeasureSpec的主要概念 https://www.jianshu.com/p/c1f8df587985
    - View 在AT_MOST的情况下，是如何精确计算出内容的高度，如TextView的文本高度；

### Scroller滑动辅助类

  - View的scrollTo()、scrollBy()方法 scrollTo、scrollBy滑动的是View中的内容（而且还是整体滑动），而不是View本身.

  - Scroller详解 https://blog.csdn.net/hehe_heh/article/details/80255289

  - View的scrollTo()、scrollBy()是瞬间完成的，当手指在屏幕上移动时，内容会跟着手指滑动，手指一旦抬起，滑动就会停止，
    如果需要实现惯性滚动过程的效果或者回弹的效果，就需要Scroller辅助类。

    Scroller本身不会移动View，它只是一个移动计算辅助类，用于跟踪空间滑动的轨迹，只相当于一个滚动轨迹记录工具，最终还是
    通过View的scrollTo、scrollBy方法完成View的移动

    Scroller的主要方法startScroll()、fling()和computeScrollOffset()方法也只是对一些轨迹参数进行设置和计算，
    真正需要进行滑动还是View的scrollTo()、scrollBy()方法；

    Scroller类的基本使用流程可以总结如下：
    （1）首先通过Scroller类的startScroll()开始一个滑动动画控制，里面进行了一些轨迹参数的设置和计算；
    （2）在调用startScroll()的后面调用invalidate()；引起视图的重绘操作，从而触发View中的computeScroll()被调用；
    （3）在computeScroll()方法中，先调用Scroller类中的computeScrollOffset()方法，里面根据当前消耗时间进行轨迹坐标的计算，
        然后取得计算出的当前滑动的偏移坐标，调用View的scrollTo()方法进行滑动控制，最后也需要调用invalidate()；进行重绘。


### View的Measure过程
   - TextView的onMeasure方法：https://baijiahao.baidu.com/s?id=1610594234856004834&wfr=spider&for=pc
   - android之TextView文字绘制流程：https://www.cnblogs.com/bvin/p/5370490.html


### Kotlin
   - 没有static 关键字，没有静态变量和静态方法

   - object 关键字
     object关键字的使用场景：https://blog.csdn.net/xlh1191860939/article/details/79460601
   - 延时加载
   - 单例模式
     Kotlin下的5种单例 https://www.jianshu.com/p/5797b3d0ebd0
   - 协程

### git push 流程：
- https://www.cnblogs.com/yorkmass/p/11109817.html