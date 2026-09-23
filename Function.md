# Command Line Function
- #wa {number}
    - 延迟 {number} 毫秒
- #{number} cmd1
    - 重复后续指令 {number} 次
- #revert （必须配合 #collect使用）
    - 往回走：从 #collect 指令开始后，走过的路径
    - 走完后，清空历史指令队列
    - 例如： #collect;w;#wa 1000;#3 w;#revert
    > 目前还不够完善，除了方向之外，想look这类的操作也会被重跑