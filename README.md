# CusHeaderViewPager 用来实现可滑动头布局的ViewPager

问题：
  在实际的项目中会遇到很多类似个人主页的情况，一个页面中包含多个Fragmeng同时还存在一个Header,
  类似RecyclerView可以滚动Header

解决方案：
 - 自定义HeaderView实现LinearLayout,处理事件事件分发和滑动；


### Measure的主要概念和流程
    - MeasureSpec的主要概念 https://www.jianshu.com/p/c1f8df587985
    - View 在AT_MOST的情况下，是如何精确计算出内容的高度，如TextView的文本高度；

### Scroller 类
  - View的scrollTo()、scrollBy()方法 scrollTo、scrollBy滑动的是View中的内容（而且还是整体滑动），而不是View本身
  - Scroller 详解 https://blog.csdn.net/hehe_heh/article/details/80255289
### git push 流程：
- https://www.cnblogs.com/yorkmass/p/11109817.html