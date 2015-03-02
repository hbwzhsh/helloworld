/**
 * Created by zqhxuyuan on 15-2-28.
 */
public class Toy {

    public static void main(String[] args) {
        String[] s1 = new String[]{
                "14092500541\t1\t1\t1\t315\t360\t洪塘路:(闽江大道至西三环)段,洪塘路番品粥店前行树池有擦车垃圾布。无路灯杆号。\t福州市仓山区建新镇洪光村5栋\t\t3\t122\t10187\t101055\t423985.4852885406.841\t2014-09-25 07:45:00\t1\t0\t1\t2015-02-28 11:56:31\t建新镇\t仓山区\t1\t1\t0\t1\t0\t\t1\t0\t0\t0\t0\t0\t2014-09-25 15:23:43\t2015-02-28 10:43:18\t6\t2014-09-26 11:53:43\t市接线员（核查）",
                "14092603132\t1\t1\t1\t315\t360\t连江北路,(化工路至福新东路),连江北路,道东,路灯号,东二环113东7米绿地脏乱。\t福州市晋安区岳峰镇连江北路297号百姓家具广场\t\t5\t137\t10330\t102287\t432899.626\t2886936.947\t2014-09-26 15:46:42\t2\t0\t1\t2015-02-28 11:56:34\t晋安区城乡建设局\t晋安区\t1\t1\t0\t1\t0\t\t1\t0\t0\t000\t2015-02-26 16:56:20\t2015-02-28 11:04:58\t6\t2015-02-27 16:26:00\t市接线员（核查）",
                        "14110500708\t1\t1\t1\t315\t357\t南二环路:十字亭路至建新南路段西侧,“坊兜人行天桥”下,积存垃圾。\t福州市仓山区建新镇建平村精神科临床康复中心\t\t3\t122\t10162\t101099\t428432.112880472.64\t2014-11-05 10:23:31\t3\t0\t1\t2015-02-28 11:56:21\t建新镇\t仓山区\t1\t1\t0\t1\t0\t\t1\t1\t0\t0\t0\t0\t2014-11-05 10:30:41\t2015-02-26 16:38:23\t19.5\t2014-11-10 10:29:00\t[办结]",
                        "14111004254\t1\t1\t1\t319\t396\t西园路:西园雅居东区大门口向南十米,占道经营,\t福州市晋安区新店镇西园社区23栋\t\t5\t138\t10366\t102004\t431885.803\t2890528.812\t2014-11-10 16:35:43\t4\t0\t1\t2015-02-28 11:56:22\t新店镇\t晋安区\t1\t0\t0\t0\t0\t\t0\t0\t0\t1\t0\t0\t\t\t3\t2015-02-28 16:37:54\t专业部门阶段",
                        "14111801324\t1\t1\t1\t316\t379\t琯后街(群众东路至状元街)段,新顶立贸易西南方向7米,占道广告牌。\t福州市台江区新港街道五一新村后巷7号五一新村\t\t2\t111\t10086\t100326\t431055.9172884999.289\t2014-11-18 08:40:20\t5\t0\t1\t2015-02-28 11:56:27\t新港街道\t台江区\t1\t1\t0\t1\t0\t\t1\t0\t0\t0\t0\t0\t2014-11-18 10:11:14\t2015-02-28 11:25:04\t19.5\t2014-11-21 10:10:00\t市接线员（核查）",
                        "14112600939\t1\t1\t1\t316\t379\t五一中路(国货西路至状元街)段,五一中路117一1号人民公社正西方向3米,占道广告牌。\t福州市台江区新港街道五一中路183号民航大厦\t\t2\t111\t10086\t100324\t430710.089\t2884795.895\t2014-11-26 08:10:51\t6\t0\t1\t2015-02-28 11:56:40\t新港街道\t台江区\t1\t1\t0\t1\t0\t\t1\t0\t0\t0\t002014-11-26 09:22:26\t2015-02-28 11:37:21\t19.5\t2014-12-01 09:21:00\t市接线员（核查）",
                        "14120101521\t1\t1\t1\t316\t377\t长春路:福马路与长春路段,长春路道西,(无路灯标识)门牌66号北侧5米处非法小广告。\t福州市晋安区鼓山镇长春路66号东榕公寓\t\t5\t141\t10420\t101835\t434109.483\t2885009.784\t2014-12-01 10:43:41\t7\t0\t1\t2015-02-28 11:56:20\t鼓山镇\t晋安区\t1\t1\t0\t1\t0\t\t1\t0\t0\t1\t0\t0\t2015-01-23 14:57:57\t2015-02-25 00:03:31\t6\t2015-01-15 16:01:00\t市接线员（核查）",
                        "14122603669\t1\t1\t1\t319\t399\t秀峰路:秀峰支路至井店路段,道东,电杆秀峰路113号南35米处,店外经营\t福州市晋安区新店镇溪里工业园1号福州市晋安区实验小学\t\t5\t138\t10345\t102486\t431915.546\t2892663.368\t2014-12-26 15:33:45\t8\t0\t1\t2015-02-28 11:56:47\t新店镇\t晋安区\t1\t0\t0\t0\t0\t\t0\t0\t0\t1\t0\t0\t\t32015-02-28 16:38:07\t专业部门阶段",
                        "14120302815\t1\t1\t1\t316\t379\t五一中路(龙庭路至古田路)段,五一中路71一5号客家牛肉风味店西北方向5米,占道广告牌。\t福州市台江区新港街道五一中路71-4号福州市土地房屋开发总公司\t\t2111\t10085\t100320\t430631.92\t2885420.972\t2014-12-03 11:36:32\t9\t0\t1\t2015-02-28 11:56:24\t新港街道\t台江区\t1\t1\t0\t1\t0\t\t1\t1\t000\t0\t2014-12-03 14:45:01\t2015-02-28 11:33:31\t19.5\t2014-12-08 11:59:00\t[办结]",
                        "14123001374\t1\t1\t1\t316\t379\t五一中路(状元街至群众东路)段,建设银行正北方向10米,占道广告牌\t福州市台江区新港街道五一中路151号华能大厦\t\t2\t111\t10084\t100316\t430673.0542884989.193\t2014-12-30 08:57:09\t10\t0\t1\t2015-02-28 11:56:48\t新港街道\t台江区\t1\t1\t0\t1\t0\t\t1\t0\t0\t0\t0\t0\t2014-12-30 09:06:00\t2015-02-28 11:27:33\t19.5\t2015-01-05 09:05:00\t市接线员（核查）"
        };

        for(String s : s1){
            System.out.println(s.split("\\t").length);
        }

        String sl = "市接线员30,市派遣员5,福州市水务管网维护有限公司,市指挥长5,市派遣员7,晋安区派遣员2,晋安区城乡建设局,晋安区指挥长, ..., ...";
        System.out.println(sl.length());
    }
}