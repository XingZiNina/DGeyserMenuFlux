# 🌟 DGeyserMenuFlux - 跨时代的多版本菜单插件

> 🚀 **为 Folia 服务器设计的一款革命性的 Minecraft 菜单插件，完美支持 Java 版与基岩版玩家**

---

## ✨ 核心特性

### 🎯 **Folia线程安全**
- **区域化调度**：使用 Folia 的 RegionScheduler 和 EntityScheduler
- **无阻塞设计**：异步任务不会阻塞主线程
- **性能优化**：充分利用 Folia 的多线程架构

### ⚡ **极致性能与体验**
- **轻量化设计**：仅核心功能，零性能负担
- **热重载配置**：修改配置即时生效，无需重启服务器
- **超大容量**：支持最多 6 行（54格）Java菜单
- **无缝迁移**：完美兼容 DeluxeMenus 配置格式

### 🛠 **开发者友好**
- **PAPI 支持**：全面支持 PlaceholderAPI 变量
- **新手友好**：简单配置，快速上手
- **丰富示例**：提供完整可用的示例菜单

### 🎁 **智能物品系统**
- **自动钟表管理**：自动检测背包中的菜单钟表，缺失时自动补充
- **死亡保护**：钟表不会因玩家死亡而丢失
- **唯一标识**：通过NBT标签确保钟表唯一性
- **便捷操作**：右键钟表即可快速打开主菜单

---

## 🚀 快速开始

### 安装步骤
1. 将插件放入 `plugins` 文件夹
2. 重启服务器
3. 在 `plugins/DGeyserMenuFlux/` 查看生成的示例菜单
4. 根据需要修改配置

### 🕰️ 智能钟表系统
插件会自动为每位玩家提供**菜单钟表**：
- ✅ **自动检测**：登录时检查背包，缺失则自动补充
- ✅ **死亡保护**：死亡不会掉落，重生后自动恢复
- ✅ **快速访问**：右键钟表立即打开主菜单
- ✅ **权限控制**：仅限拥有 `dgeysermenu.use` 权限的玩家

**管理员命令**：
```bash
/getmenuclock  # 手动获取菜单钟表


### 📋 基础命令
```bash
# 打开指定菜单
/dgeysermenu open <菜单名称>

# 重载插件配置
/dgeysermenu reload [all|java|bedrock]

# 查看可用菜单
/dgeysermenu list

# 获取菜单钟表（管理员）
/getmenuclock
```

### 📋 简短命令
```bash
# 打开指定菜单
/dgm open <菜单名称>

# 重载插件配置
/dgm reload [all|java|bedrock]

# 查看可用菜单
/dgm list
```

### 🔐 权限节点
```yaml
# 基础使用权限
dgeysermenu.use

# 管理员权限
dgeysermenu.admin

# 特定菜单权限
dgeysermenu.menu.<菜单名称>

# 重载权限
dgeysermenu.reload
```

---

## 📖 配置详解

### Java 版菜单配置 (java_menus/)
```yaml
# 基础设置
menu_title: "&6&l主菜单"  # 菜单标题（支持颜色代码）
rows: 6                   # 菜单行数（1-6）

# 物品配置示例
items:
  info_item:              # 物品唯一ID
    slot: 11              # 槽位（0-53）或范围 "0-8"
    material: PAPER       # 材质名称
    display_name: "&e&l信息"  # 显示名称
    lore:                 # 描述文本
      - "&7点击查看服务器信息"
      - "&f在线: &a%server_online%"
  
    # 命令配置
    left_click_commands:  # 左键点击命令
      - "[message] &a欢迎使用菜单!"
      - "[player] spawn"
    right_click_commands: # 右键点击命令
      - "[close]"
```

### 基岩版菜单配置 (bedrock_menus/)
```yaml
menu:
  title: "主菜单"         # 表单标题
  subtitle: "欢迎使用"     # 副标题
  footer: "服务器名称"     # 页脚文本

  items:
    - text: "服务器信息"    # 按钮文本
      icon: "textures/items/paper"  # 图标路径
      icon_type: "path"    # 图标类型 (path/url)
      command: "dgeysermenu open info"  # 执行命令
      execute_as: "player" # 执行身份 (player/console)
```

```yaml
# 配置示例 - config.yml
settings:
  menu-clock:
    enabled: true           # 启用钟表系统
    auto-give: true         # 自动给予
    death-protection: true  # 死亡保护
```

---

## 🎨 示例菜单详解

### 🏠 主菜单配置分析

#### Java 版主菜单 (main.yml)
```yaml
menu_title: "&6&l主菜单 &7| &f服务器名称"
rows: 6

items:
  # 服务器信息区域
  server_info:
    slot: 10
    material: KNOWLEDGE_BOOK        # 知识书材质，象征信息
    display_name: "&e&l服务器信息"    # 黄色突出显示
    lore:                           # 动态显示服务器数据
      - "&7点击查看服务器信息"
      - ""
      - "&f在线玩家: &a%server_online%&f/&a%server_max_players%"
      - "&fTPS: &a%server_tps%"
    left_click_commands:
      - "[message] &6=== 服务器信息 ==="
      - "[message] &f在线玩家: &a%server_online%&f/&a%server_max_players%"

  # 传送功能
  teleport_spawn:
    slot: 12
    material: COMPASS               # 指南针材质，象征导航
    display_name: "&a&l传送回城"     # 绿色表示安全传送
    lore:
      - "&7点击传送至主城"
      - ""
      - "&f立即传送至安全的主城区域"
    left_click_commands:
      - "[player] spawn"            # 执行传送命令
      - "[message] &a已传送至主城!"  # 反馈信息

  # 装饰边框系统
  border_top:
    slot: 0-8                      # 范围槽位，顶部边框
    material: BLACK_STAINED_GLASS_PANE  # 黑色染色玻璃板
    display_name: " "               # 空名称，纯装饰
```

#### 基岩版主菜单 (bedrock_main.yml)
```yaml
menu:
  title: "§6§l主菜单"
  subtitle: "§7欢迎来到服务器!"
  footer: "§8服务器版本 1.21.x"

  items:
    - text: "§e§l服务器信息\n§7点击查看服务器状态"
      icon: "textures/items/book_enchanted"  # 附魔书图标
      icon_type: "path"
      command: "dgeysermenu open info"
    
    - text: "§a§l传送功能\n§7快速传送至各个地点"
      icon: "textures/items/compass_item"    # 指南针图标
      icon_type: "path"
      command: "dgeysermenu open warps"
```

---

## 🔄 从 DeluxeMenus 迁移

### 配置对比表
| DeluxeMenus | DGeyserMenuFlux | 说明 |
|-------------|-----------------|------|
| `menu_title` | `menu_title` | 完全兼容 |
| `rows` | `rows` | 完全兼容 |
| `items.<id>.slot` | `items.<id>.slot` | 完全兼容 |
| `items.<id>.material` | `items.<id>.material` | 完全兼容 |
| `items.<id>.display_name` | `items.<id>.display_name` | 完全兼容 |
| `items.<id>.lore` | `items.<id>.lore` | 完全兼容 |
| `items.<id>.left_click_commands` | `items.<id>.left_click_commands` | 完全兼容 |
| `items.<id>.right_click_commands` | `items.<id>.right_click_commands` | 完全兼容 |

### 📊 特性对比表

| 特性 | DGeyserMenuFlux | 传统菜单插件 |
|------|-----------------|--------------|
| 自动物品管理 | ✅ 智能钟表系统 | ❌ 手动发放 |
| 热重载支持 | ✅ 即时生效 | ⚠️ 需要重载 |
| 多版本原生支持 | ✅ Java+基岩版 | ❌ 仅单版本 |
| 迁移便利性 | ✅ 完美兼容 | ❌ 重新配置 |
| 性能表现 | ✅ 轻量高效 | ⚠️ 资源消耗大 |


### 迁移步骤
1. **复制配置文件**：将 DeluxeMenus 的 YAML 文件复制到 `java_menus/` 文件夹
2. **调整路径**：确保文件路径正确
3. **测试功能**：使用 `/dgeysermenu open <菜单名>` 测试
4. **基岩版适配**：根据需要创建对应的基岩版菜单

### 迁移示例
**DeluxeMenus 配置**：
```yaml
menu-title: "&c主菜单"
menu-rows: 3
items:
  test_item:
    slot: 13
    material: DIAMOND
    display-name: "&b测试物品"
    left-click-commands:
      - "[player] say 测试命令"
```

**DGeyserMenuFlux 配置**：
```yaml
menu_title: "&c主菜单"
rows: 3
items:
  test_item:
    slot: 13
    material: DIAMOND
    display_name: "&b测试物品"
    left_click_commands:
      - "[player] say 测试命令"
```

---

## 💡 高级技巧

### PAPI 变量使用
```yaml
items:
  player_stats:
    slot: 22
    material: PLAYER_HEAD
    display_name: "&b%player_name%"
    lore:
      - "&f等级: &e%player_level%"
      - "&f金钱: &6%vault_eco_balance%"
      - "&f在线: &b%player_time_hours%小时"
```

### 条件命令执行
```yaml
left_click_commands:
  - "[message] &6=== 条件测试 ==="
  - "[console] effect give %player_name% speed 30 1"
  - "[message] &a已获得速度效果!"
```

### 嵌套菜单系统
```yaml
left_click_commands:
  - "[menu] shop"          # 打开商店菜单
  - "[message] &2欢迎光临商店!"
```

---

## 🛠 故障排除

### 常见问题
**Q: 菜单无法打开**
- ✅ 检查权限设置 `dgeysermenu.use`
- ✅ 确认菜单文件在正确的文件夹
- ✅ 检查 YAML 语法是否正确

**Q: 命令不执行**
- ✅ 确认命令格式正确
- ✅ 检查玩家是否有执行命令的权限
- ✅ 查看控制台错误信息

**Q: 基岩版玩家看不到菜单**
- ✅ 确认 Floodgate 正确安装
- ✅ 检查基岩版菜单配置路径

**Q: PAPI 变量不显示**
- ✅ 确认 PlaceholderAPI 已安装
- ✅ 检查变量名称拼写正确

### 调试模式
在 `config.yml` 中启用调试模式：
```yaml
settings:
  debug: true
  hot-reload:
    enabled: true
```

## 🎉 开始使用

立即下载 DGeyserMenuFlux，为您的服务器带来革命性的菜单体验！

```bash
# 快速命令测试
/dgeysermenu open main
/getmenuclock
/dgeysermenu reload
```

**🎯 完美支持多版本，极致性能体验，让每个玩家都享受最佳交互！**

---
*⭐ 如果这个插件对您有帮助，请给我们一个 Star！*
