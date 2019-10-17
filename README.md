MapReduce编程尝试
===================
文件夹中的`output`为导出的jar包<br>
`input`文件夹为输入数据<br>
## IDEA的环境配置
`都是网上的教程，自己整合一下`<br>
[linux下配置IDEA](https://www.polarxiong.com/archives/Hadoop-Intellij%E7%BB%93%E5%90%88Maven%E6%9C%AC%E5%9C%B0%E8%BF%90%E8%A1%8C%E5%92%8C%E8%B0%83%E8%AF%95MapReduce%E7%A8%8B%E5%BA%8F-%E6%97%A0%E9%9C%80%E6%90%AD%E8%BD%BDHadoop%E5%92%8CHDFS%E7%8E%AF%E5%A2%83.html)
<br>
参数编辑时，需要选择Application.
<br>
[windows下配置IDEA](https://blog.csdn.net/akisaya_/article/details/78724720)
<br>
这里新建的`resources`文件夹，我测试了一下，貌似不建也可以。再有就是winutils 其实也不用完全对应，稍微错开一些版本关系也不是很大。我这里给出的winutils与我的2.8版本的hadoop一样适配。[github——winutils](https://github.com/steveloughran/winutils)
<br>
作为一个菜鸡，IDEA的jar包生成方式其实是有坑的，也参考了一个相对来说比较详细的教程。[jar包生成方法](http://www.cnblogs.com/blog5277/p/5920560.html)
<br>
## Jar包提交方式
#### bin/hadoop jar `jar包地址` `输入数据地址`（应当事先上传至hdfs） `输出数据地址`（output实现应当不存在）
若出现上传不到hdfs：<br>
1:datanode失效（使用jps查看）[教程](https://blog.csdn.net/Islotus/article/details/78357857)<br>
2:文件权限不对[教程](https://www.cnblogs.com/jdonson/archive/2011/04/28/2031878.html)
<br>
若出现找不到提示找不到main函数，网上说法很多，但是一般是打包方式不对，建议跟着教程再打包一次。<br>

### spark的方式，等下次吧。

### Notices！！！
main函数中，必须使用throws Exception，否则waitforCompletion会报错！
### spark的方式，等下次吧。
