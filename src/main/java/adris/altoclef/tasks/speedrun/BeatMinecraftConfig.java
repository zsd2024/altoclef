package adris.altoclef.tasks.speedrun;

/**
 * 速通Minecraft配置类
 * 包含完成游戏所需的各项配置参数
 */
public class BeatMinecraftConfig {
    /** 目标末影之眼数量（需要收集多少个） */
    public int targetEyes = 14;
    
    /** 最小末影之眼数量（假设末地要塞传送门还未打开时的最小持有量） */
    public int minimumEyes = 12;
    
    /** 是否在末地传送门附近设置重生点 */
    public boolean placeSpawnNearEndPortal = false;
    
    /** 是否通过与猪灵交易获取末影珍珠而不是猎杀末影人 */
    public boolean barterPearlsInsteadOfEndermanHunt;
    
    /** 是否跳过夜晚（通过睡觉） */
    public boolean sleepThroughNight = false;
    
    /** 是否重新拾取工作台 */
    public boolean rePickupCraftingTable = true;
    
    /** 食物单位数量（用于维持饱食度） */
    public int foodUnits = 220;
    
    /** 所需床的数量 */
    public int requiredBeds = 10;
    
    /** 最小建筑材料数量 */
    public int minBuildMaterialCount = 5;
    
    /** 建筑材料数量 */
    public int buildMaterialCount = 64;
    
    /** 是否重新拾取烟熏炉 */
    public boolean rePickupSmoker = true;
    
    /** 是否重新拾取熔炉 */
    public boolean rePickupFurnace = true;
}
