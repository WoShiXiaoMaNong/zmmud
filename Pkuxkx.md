# Pkuxkx

## 📊 GMCP
- 客户端开启GMCP后（application.yml)，依旧无法收到GMCP消息：
   - 在游戏重订阅GMCP Channel：
      - Buff
      - Message
      - Move
      - Combat
      - Status
   > 游戏指令，例如开启 Status消息推送：  tune GMCP Status on


## 📊 GMCP 数据  [点击查看 北大侠客行GMCP数据说明](https://www.pkuxkx.net/wiki/robot/gmcp)
### 1. 角色状态模块 (GMCP.Status)

当玩家登录角色、跨越地图、遭遇战斗或状态发生改变时，服务器会主动推送 `GMCP.Status` 模块的数据。以下为北大侠客行（PKUXKX）服务器返回的真实字段定义。

#### 📜 气血与精神
* **`qi`**: 当前气血（生命值）。
* **`eff_qi`**: 当前有效气血上限（受内伤后会降低）。
* **`max_qi`**: 基础气血上限。
* **`jing`**: 当前精气（精神值）。
* **`eff_jing`**: 当前有效精气上限。
* **`max_jing`**: 基础精气上限。

#### 📜 内力与精力
* **`neili`**: 当前内力值。
* **`max_neili`**: 当前内力上限。
* **`jingli`**: 当前精力值。
* **`max_jingli`**: 当前精力上限。

#### 📜 温饱与生存
* **`food`**: 当前食物度 / 饱食度（数值减小代表饥饿）。
* **`water`**: 当前饮水度 / 口渴度（数值减小代表口渴）。

#### 📜 角色基础身份
* **`id`**: 玩家角色的英文唯一标识（ID），例如：`mrkkk`。
* **`name`**: 玩家角色的中文名字，例如：`武源`。
* **`title`**: 玩家当前佩戴的江湖称号，例如：`普通百姓`。
* **`level`**: 角色当前等级。
* **`combat_exp`**: 角色当前拥有的实战经验值（武功评级核心指标）。
* **`family/family_name`**: 玩家所属的门派名称。若未拜师，则返回 `null`。

#### 📜 元气与特殊变量
为了保持 JSON 结构的扁平化，服务器对嵌套属性采用了 `斜杠 (/)` 进行路径拼接：
* **`vigour/qi`**: 元气系统中的气血相关分支。
* **`vigour/yuan`**: 角色当前的元气值。

---
···
package zm.mud.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;

import java.util.HashMap;
import java.util.Map;

@Component
public class Demo {

    private static final Logger logger = LogManager.getLogger(Demo.class);

    @Autowired
    private GMCPContext gmcpContext;

    public void demo() {
      
        if (statusSnapshot.isEmpty()) {
            logger.warn("【Demo】当前 GMCPContext 中的状态 Map 为空，可能尚未登录角色。");
            return;
        }

        try {
            // ================== 2. 直接从扁平的 Map 中提取原始的键值对 ==================
            // 使用自定义的快捷工具方法进行安全的数据提取与默认值处理
            String name = getStr(statusSnapshot, "name", "未知");
            String id = getStr(statusSnapshot, "id", "未知");
            String title = getStr(statusSnapshot, "title", "普通百姓");
            String level = getStr(statusSnapshot, "level", "0");

            String qi = getStr(statusSnapshot, "qi", "0");
            String maxQi = getStr(statusSnapshot, "max_qi", "0");
            String effQi = getStr(statusSnapshot, "eff_qi", "0");

            String jing = getStr(statusSnapshot, "jing", "0");
            String maxJing = getStr(statusSnapshot, "max_jing", "0");
            String effJing = getStr(statusSnapshot, "eff_jing", "0");

            String neili = getStr(statusSnapshot, "neili", "0");
            String maxNeili = getStr(statusSnapshot, "max_neili", "0");
            String jingli = getStr(statusSnapshot, "jingli", "0");
            String maxJingli = getStr(statusSnapshot, "max_jingli", "0");

            String food = getStr(statusSnapshot, "food", "0");
            String water = getStr(statusSnapshot, "water", "0");
            
            // 直接读取带有斜杠的原始扁平键名
            String familyName = getStr(statusSnapshot, "family/family_name", "无门派(散人)");
            String combatExp = getStr(statusSnapshot, "combat_exp", "0");
            String vigourYuan = getStr(statusSnapshot, "vigour/yuan", "0");
            String vigourQi = getStr(statusSnapshot, "vigour/qi", "0");

            // ================== 3. 通过 logger.info 原始映射输出 ==================
            logger.info("================= 玩家状态数据明细 =================");
            logger.info("基本信息 -> 名字: {} | ID: {} | 称号: {} | 等级: {}", name, id, title, level);
            logger.info("气血状态 -> 当前气血: {} / 基础上限: {} (有效上限: {})", qi, maxQi, effQi);
            logger.info("精神状态 -> 当前精气: {} / 基础上限: {} (有效上限: {})", jing, maxJing, effJing);
            logger.info("内力精力 -> 内力: {} / 上限: {} | 精力: {} / 上限: {}", neili, maxNeili, jingli, maxJingli);
            logger.info("生存温饱 -> 食物度: {} | 饮水度: {}", food, water);
            logger.info("门派武功 -> 门派: {} | 实战经验: {}", familyName, combatExp);
            logger.info("元气系统 -> 元气值: {} | 元气气血: {}", vigourYuan, vigourQi);
            logger.info("====================================================");

        } catch (Exception e) {
            logger.error("【Demo】读取扁平 GMCP 状态变量时发生异常", e);
        }

        // ================== 4. 提取当前房间位置 ==================
        PkuxkxRoom currentRoom = gmcpContext.getCurrentRoom();
        if (currentRoom != null) {
            logger.info("【当前位置】您目前身处: {}，可行走出口: {}", 
                    currentRoom.getRoomShort(), currentRoom.getDir());
        }
    }

    /**
     * 针对 Map 扁平提取的防御性安全转换工具方法
     */
    private String getStr(Map<String, Object> map, String key, String defaultValue) {
        Object val = map.get(key);
        if (val == null) {
            return defaultValue;
        }
        return String.valueOf(val).trim();
    }
}

···