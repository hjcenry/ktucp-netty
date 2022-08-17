package com.hjcenry.util;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 现有方法如下： 1.指定字符串替换:String replace(String strSc, String oldStr, String newStr)
 * 2.将字符串数组以指定的字符进行连接:String join(String[] strs, String token)
 * 3.检查字符串中是否包含某字符，包含返回true ：check(String str, String test) 4.将字符串转换成Integer型:
 * Integer String2Integer(String str, Integer ret) 5.将数值型转换成字符串:String
 * Integer2String(Integer it, String ret) 6.比较两字符串大小(ASCII码顺序)：int
 * compare(String str1, String str2) 7.将字符串的首字母改为大写:String firstToUpper(String
 * str) 8.检查字符串是否为空 :boolean isEmpty(String str) 9.截取并保留标志位之前的字符串 :String
 * substringAfter(String str, String expr) 10.截取并保留标志位之后的字符串 ：String
 * substringAfter(String str, String expr) 11.截取并保留最后一个标志位之前的字符串 :String
 * substringBeforeLast(String str, String expr) 12.截取并保留最后一个标志位之后的字符串 :String
 * substringAfterLast(String str, String expr) 13.返回一个整数数组 ：int[] split(String
 * s,String spliter) 14.返回一个整数数组 :int[] parseInt(String[] s)
 * 15.字符串数组中是否包含指定的字符串: boolean contains(String[] strings, String string,
 * boolean caseSensitive) 16.将字串转成日期，字串格式: yyyy-MM-dd ：Date parseDate(String
 * string) 17.字符填充(向前填充) :String fill(String source,String filler, int length)
 * 18.转换String到boolean ：boolean parseBoolean(String flag)a 19.转换String到int : int
 * parseInt(String flag) 20.转换String到long :long parseLong(String flag)
 * 21.改变字符串编码到gbk :String toGBK(String src) 22.改变字符串编码到UTF8 :String
 * toUTF8(String src) 23.截取字符串(最后位置的指定字符到字符结尾)
 */
public class StringUtil {

    public static final String REGEX = "@@"; //链接符

    /***
     * 判断 String 是否是 int
     *
     * @param input
     * @return
     */
    public static boolean isInteger(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        Matcher mer = Pattern.compile("^[+-]?[0-9]+$").matcher(input);
        return mer.find();
    }

    /**
     * 替换字符串，修复java.lang.String类的replaceAll方法时第一参数是字符串常量正则时(如："address".
     * replaceAll("dd","$");)的抛出异常：java.lang.StringIndexOutOfBoundsException:
     * String index out of range: 1的问题。
     *
     * @param strSc  思路是重构字符串 需要进行替换的字符串
     * @param oldStr 源字符串
     * @param newStr 替换后的字符串
     * @return 替换后对应的字符串
     * @since 1.2
     */
    public static String replace(String strSc, String oldStr, String newStr) {
        int i = -1;
        while ((i = strSc.indexOf(oldStr)) != -1) {
            strSc = new StringBuffer(strSc.substring(0, i)).append(newStr)
                    .append(strSc.substring(i + oldStr.length())).toString();
        }
        return strSc;
    }

    /**
     * 将一字符串数组以某特定的字符串作为分隔来变成字符串
     *
     * @param strs  字符串数组
     * @param token 分隔字符串
     * @return 以token为分隔的字符串
     * @since 1.0
     */
    public static String join(String[] strs, String token) {
        if (strs == null)
            return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strs.length; i++) {
            if (i != 0)
                sb.append(token);
            sb.append(strs[i]);
        }
        return sb.toString();
    }

    /**
     * 验证字符串合法性
     *
     * @param str  需要验证的字符串
     * @param test 非法字符串（如："~!#$%^&*()',;:?"）
     * @return true:非法;false:合法
     * @since 1.0
     */
    public static boolean check(String str, String test) {
        if (str == null || str.equals(""))
            return true;
        boolean flag = false;
        for (int i = 0; i < test.length(); i++) {
            if (str.indexOf(test.charAt(i)) != -1) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 将数值型字符串转换成Integer型
     *
     * @param str 需要转换的字符型字符串
     * @param ret 转换失败时返回的值
     * @return 成功则返回转换后的Integer型值；失败则返回ret
     * @since 1.0
     */
    public static int String2Integer(String str, Integer ret) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return ret;
        }
    }

    /**
     * 将数值型转换成字符串
     *
     * @param it  需要转换的Integer型值
     * @param ret 转换失败的返回值
     * @return 成功则返回转换后的字符串；失败则返回ret
     * @since 1.0
     */
    public static String Integer2String(Integer it, String ret) {
        try {
            return Integer.toString(it);
        } catch (NumberFormatException e) {
            return ret;
        }
    }

    /**
     * 比较两字符串大小(ASCII码顺序)
     *
     * @param str1 参与比较的字符串1
     * @param str2 参与比较的字符串2
     * @return str1>str2:1;str1<str2:-1;str1=str2:0
     * @since 1.1
     */
    public static int compare(String str1, String str2) {//
        if (str1.equals(str2)) {
            return 0;
        }
        int str1Length = str1.length();
        int str2Length = str2.length();
        int length = 0;
        if (str1Length > str2Length) {
            length = str2Length;
        } else {
            length = str1Length;
        }
        for (int i = 0; i < length; i++) {
            if (str1.charAt(i) > str2.charAt(i)) {
                return 1;
            }
        }
        return -1;
    }

    /**
     * 将字符串的首字母改为大写
     *
     * @param str 需要改写的字符串
     * @return 改写后的字符串
     * @since 1.2
     */
    public static String firstToUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将字符串的首字母改为小写
     *
     * @param str 需要改写的字符串
     * @return 改写后的字符串
     * @since 1.2
     */
    public static String firstToLower(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 默认的空值
     */

    public static final String EMPTY = "";

    /**
     * 检查字符串是否为空
     *
     * @param str 字符串
     * @return
     */

    public static boolean isNotEmpty(String str) {
        if (str == null) {
            return false;
        } else if (str.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        } else if (str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * jdbc专用判断函数
     */
    public static boolean isMySqlEmpty(String str) {
        boolean back = isEmpty(str);
        if (!back && str.equalsIgnoreCase("null")) {
            back = true;
        }
        return back;
    }


    /**
     * 截取并保留标志位之前的字符串
     *
     * @param str  字符串
     * @param expr 分隔符
     * @return
     */

    public static String substringBefore(String str, String expr) {
        if (isEmpty(str) || expr == null) {
            return str;
        }
        if (expr.length() == 0) {
            return EMPTY;
        }
        int pos = str.indexOf(expr);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);

    }

    /**
     * 截取并保留标志位之后的字符串
     *
     * @param str  字符串
     * @param expr 分隔符
     * @return
     */

    public static String substringAfter(String str, String expr) {
        if (isEmpty(str)) {
            return str;
        }
        if (expr == null) {
            return EMPTY;
        }
        int pos = str.indexOf(expr);
        if (pos == -1) {
            return EMPTY;
        }
        return str.substring(pos + expr.length());
    }

    /**
     * 截取并保留最后一个标志位之前的字符串
     *
     * @param str  字符串
     * @param expr 分隔符
     * @return
     */

    public static String substringBeforeLast(String str, String expr) {
        if (isEmpty(str) || isEmpty(expr)) {
            return str;
        }
        int pos = str.lastIndexOf(expr);
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * 截取并保留最后一个标志位之后的字符串
     *
     * @param str
     * @param expr 分隔符
     * @return
     */
    public static String substringAfterLast(String str, String expr) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(expr)) {
            return EMPTY;
        }
        int pos = str.lastIndexOf(expr);
        if (pos == -1 || pos == (str.length() - expr.length())) {
            return EMPTY;
        }
        return str.substring(pos + expr.length());
    }

    /**
     * 返回一个整数数组
     *
     * @param s       String
     * @param spliter 分隔符如逗号
     * @return int[]
     */
    public static int[] splitInt(String s, String spliter) {
        if (s == null || s.indexOf(spliter) == -1) {
            return (new int[0]);
        }
        String[] ary = s.split(spliter);
        int[] result = new int[ary.length];
        try {
            for (int i = 0; i < ary.length; i++) {
                result[i] = Integer.parseInt(ary[i]);
            }
        } catch (NumberFormatException ex) {
        }
        return result;
    }

    /**
     * 分割字符串。不同于{@link String#split(String)}，本方法不用正则匹配。
     *
     * @param str       要分割的字符串
     * @param delimiter 分隔符
     * @return 分割后的字符串数组
     */
    public static String[] split(String str, String delimiter) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[]{str};
        }
        List<String> result = new ArrayList<String>();
        if ("".equals(delimiter)) {
            for (int i = 0; i < str.length(); i++) {
                result.add(str.substring(i, i + 1));
            }
        } else {
            int pos = 0;
            int delPos = 0;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(str.substring(pos, delPos));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(str.substring(pos));
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * 返回一个整数数组
     *
     * @param s String[]
     * @return int[]
     */
    public static int[] parseInt(String[] s) {
        if (s == null) {
            return (new int[0]);
        }
        int[] result = new int[s.length];
        try {
            for (int i = 0; i < s.length; i++) {
                result[i] = Integer.parseInt(s[i]);
            }
        } catch (NumberFormatException ex) {
        }
        return result;
    }

    /**
     * 字符串数组中是否包含指定的字符串。
     *
     * @param strings       字符串数组
     * @param string        字符串
     * @param caseSensitive 是否大小写敏感
     * @return 包含时返回true，否则返回false
     */

    public static boolean contains(String[] strings, String string,
                                   boolean caseSensitive) {
        for (int i = 0; i < strings.length; i++) {
            if (caseSensitive == true) {
                if (strings[i].equals(string)) {
                    return true;
                }
            } else {
                if (strings[i].equalsIgnoreCase(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将字串转成日期，字串格式: yyyy-MM-dd
     *
     * @param string String
     * @return Date
     */

    public static Date parseDate(String string) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return (Date) formatter.parse(string);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 字符填充
     *
     * @param source 源字符串
     * @param filler 填充字符,如0或*等
     * @param length 最终填充后字符串的长度
     * @return 最终填充后字符串
     */
    public static String fill(String source, String filler, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length - source.length(); i++) {
            sb.append(filler);
        }
        sb.append(source);
        return sb.toString();
    }

    /**
     * 转换String到boolean
     */
    public static boolean parseBoolean(String flag) {
        if (isEmpty(flag))
            return false;
        else if (flag.equals("true") || flag.equals("1") || flag.equals("是")
                || flag.equals("yes"))
            return true;
        else if (flag.equals("false") || flag.equals("0") || flag.equals("否")
                || flag.equals("no"))
            return false;
        return false;
    }

    /**
     * 转换String到int <br>
     * null或空字符,返回0 <br>
     * true返回1 <br>
     * false返回0
     */
    public static int parseInt(String flag) {
        if (isEmpty(flag))
            return 0;
        else if (flag.equals("true"))
            return 1;
        else if (flag.equals("false"))
            return 0;
        return Integer.parseInt(flag);
    }

    /**
     * 转换String到int <br>
     * null或空字符,返回0 <br>
     * true返回1 <br>
     * false返回0
     */
    public static int parseInt(String flag, int defaultValue) {
        if (isEmpty(flag))
            return defaultValue;
        else if (flag.equals("true"))
            return 1;
        else if (flag.equals("false"))
            return 0;
        if (isInteger(flag))
            return Integer.parseInt(flag);
        else {
            return defaultValue;
        }
    }

    /**
     * 转换String到long
     */
    public static long parseLong(String flag) {
        if (isEmpty(flag))
            return 0;
        return Long.parseLong(flag);
    }


    /**
     * 改变字符串编码到gbk
     */
    public static String toGBK(String src) {
        if (isEmpty(src))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes("ISO-8859-1"), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 改变字符串编码到utf8
     */
    public static String toUTF8(String src) {
        if (isEmpty(src))
            return "";
        String s = null;
        try {
            s = new String(src.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 判断字符串数组是否为空
     */
    public static boolean nil(String[] s) {
        return (s == null || s.length == 0);
    }

    /**
     * 截取字符串(最后位置的指定字符到字符结尾)
     *
     * @param str
     * @param charset
     * @return
     */
    public static String getLastString(String str, String charset) {
        return str.substring(str.lastIndexOf(".") + 1);
    }

    /**
     * 获取唯一标识符
     *
     * @param pre
     * @param last
     * @return
     */
    public static String getUniqueId(String pre, String last) {
        UUID uuid = UUID.randomUUID();
        return pre + uuid.toString() + last;
    }

    /**
     * 字符串中是否包含中文
     *
     * @param s
     * @return
     */
    public static boolean containsCn(String s) {
        if (s != null) {
            Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
            Matcher matcher = pattern.matcher(s);
            return matcher.find();
        }
        return false;
    }

    /**
     * 将字符串的指定位置替换成新的字符
     *
     * @param str     原字符串
     * @param n       指定要替换的位数
     * @param newChar 要替换的字符
     * @return String 替换后的字符串
     * @throws Throwable
     */
    public static String replace(String str, int n, String newChar)
            throws Exception {
        String s1 = "";
        String s2 = "";

        s1 = str.substring(0, n - 1);
        s2 = str.substring(n, str.length());

        return s1 + newChar + s2;
    }


    /**
     * 验证sql注入
     *
     * @param str
     * @return
     */
    public static boolean sqlValidate(String str) {
        str = str.toLowerCase();//统一转为小写
        String badStr = "exec|execute|insert|select|delete|update|count|drop|mid|master|truncate|" +
                "char|declare|sitename|net user|xp_cmdshell|like'|and|exec|execute|insert|create|drop|" +
                "table|from|grant|use|group_concat|column_name|" +
                "information_schema.columns|table_schema|union|where|select|delete|update|order|count|" +
                "mid|master|truncate|char|declare|--|like|//|/";//过滤掉的sql关键字，可以手动添加
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.contains(badStrs[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取百家姓
     *
     * @return
     */
    public static String getBaiJiaXing() {
        String baijiaxingString = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨" +
                "朱秦尤许何吕施张" +
                "孔曹严华金魏陶姜" +
                "戚谢邹喻柏水窦章" +
                "云苏潘葛奚范彭郎" +
                "鲁韦昌马苗凤花方" +
                "俞任袁柳酆鲍史唐" +
                "费廉岑薛雷贺倪汤" +
                "滕殷罗毕郝邬安常" +
                "乐于时傅皮卞齐康" +
                "伍余元卜顾孟平黄" +
                "和穆萧尹姚邵堪汪" +
                "祁毛禹狄米贝明臧" +
                "计伏成戴谈宋茅庞" +
                "熊纪舒屈项祝董粱" +
                "杜阮蓝闵席季麻强" +
                "贾路娄危江童颜郭" +
                "梅盛林刁钟徐邱骆" +
                "高夏蔡田樊胡凌霍" +
                "虞万支柯咎管卢莫" +
                "经房裘缪干解应宗" +
                "宣丁贲邓郁单杭洪" +
                "包诸左石崔吉钮龚" +
                "程嵇邢滑裴陆荣翁" +
                "荀羊於惠甄魏加封" +
                "芮羿储靳汲邴糜松" +
                "井段富巫乌焦巴弓" +
                "牧隗山谷车侯宓蓬" +
                "全郗班仰秋仲伊宫" +
                "宁仇栾暴甘钭厉戎" +
                "祖武符刘姜詹束龙" +
                "叶幸司韶郜黎蓟薄" +
                "印宿白怀蒲台从鄂" +
                "索咸籍赖卓蔺屠蒙" +
                "池乔阴郁胥能苍双" +
                "闻莘党翟谭贡劳逄" +
                "姬申扶堵冉宰郦雍" +
                "郤璩桑桂濮牛寿通" +
                "边扈燕冀郏浦尚农" +
                "温别庄晏柴瞿阎充" +
                "慕连茹习宦艾鱼容" +
                "向古易慎戈廖庚终" +
                "暨居衡步都耿满弘" +
                "匡国文寇广禄阙东" +
                "殴殳沃利蔚越夔隆" +
                "师巩厍聂晁勾敖融" +
                "冷訾辛阚那简饶空" +
                "曾毋沙乜养鞠须丰" +
                "巢关蒯相查后江红" +
                "游竺权逯盖益桓公" +
                "万俟司马上官欧阳" +
                "夏侯诸葛闻人东方" +
                "赫连皇甫尉迟公羊" +
                "澹台公冶宗政濮阳" +
                "淳于仲孙太叔申屠" +
                "公孙乐正轩辕令狐" +
                "钟离闾丘长孙慕容" +
                "鲜于宇文司徒司空" +
                "亓官司寇仉督子车" +
                "颛孙端木巫马公西" +
                "漆雕乐正壤驷公良" +
                "拓拔夹谷宰父谷粱" +
                "晋楚闫法汝鄢涂钦" +
                "段干百里东郭南门" +
                "呼延妫海羊舌微生" +
                "岳帅缑亢况後有琴" +
                "梁丘左丘东门西门" +
                "商牟佘佴伯赏南宫" +
                "墨哈谯笪年爱阳佟" +
                "第五言福兰芦渠付" +
                "仝肖覃";
        return baijiaxingString;
    }


    public static int chineseNum2Num(String chineseNum) {
        int num = 0;
        switch (chineseNum) {
            case "一":
                num = 1;
                break;
            case "二":
                num = 2;
                break;
            case "三":
                num = 3;
                break;
            case "四":
                num = 4;
                break;
            case "五":
                num = 5;
                break;
            case "六":
                num = 6;
                break;
            case "七":
                num = 7;
                break;
            case "八":
                num = 8;
                break;
            case "九":
                num = 9;
                break;
            case "十":
                num = 10;
                break;
            case "十一":
                num = 11;
                break;
            case "十二":
                num = 12;
                break;
            case "十三":
                num = 13;
                break;
            case "十四":
                num = 14;
                break;
        }

        return num;
    }

    /**
     * 编码platform+account
     *
     * @param platform
     * @param account
     * @return
     */
    public static String encodePlatformKey(String platform, String account) {
        return new StringBuilder().append(platform).append(REGEX).append(account).toString();
    }

    /**
     * 验证特殊字符
     *
     * @param str
     * @return
     */
    public static boolean filter(String str) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    /**
     * 返回字符串字节数量
     */
    public static int getStringbyte(String str) {

        int byteLength = 0;

        if (str == null || str.length() < 1) {
            return byteLength;
        }

        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char cn = charArray[i];
            byte[] bytes = (String.valueOf(cn)).getBytes();
            if (bytes.length == 1) {
                //英文字符
                byteLength++;
            } else {
                //中文字符
                byteLength += 2;
            }
        }
        return byteLength;
    }

    /**
     * 大写转小写下划线
     *
     * @param filed 字符串
     */
    public static String fieldToColumn(String filed) {

        if (StringUtil.isEmpty(filed)) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder(filed);
            int count = 0;
            builder.replace(0, 1, (filed.charAt(0) + "").toLowerCase());

            for (int i = 1; i < filed.length(); ++i) {
                char c = filed.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    builder.replace(i + count, i + count + 1, (c + "").toLowerCase());
                    builder.insert(i + count, "_");
                    ++count;
                }
            }
            return builder.toString();
        }
    }


    /**
     * 判断字符串是否未基础类型
     */
    public static boolean isPrimitive(String type) {
        if (type.endsWith("Boolean")) {
            return true;
        } else if (type.endsWith("Character")) {
            return true;
        } else if (type.endsWith("Byte")) {
            return true;
        } else if (type.endsWith("Short")) {
            return true;
        } else if (type.endsWith("Integer")) {
            return true;
        } else if (type.endsWith("Long")) {
            return true;
        } else if (type.endsWith("Float")) {
            return true;
        } else if (type.endsWith("Double")) {
            return true;
        } else if (type.endsWith("Void")) {
            return true;
        }
        return false;
    }

    public static String trim(String str) {
        if (str == null) {
            str = "";
        } else {
            str = str.trim();
        }
        if (str.length() == 0) {
            return str;
        }

        if (str.charAt(0) == '"') {
            str = str.substring(1);
        }

        if (str.charAt(str.length() - 1) == '"') {
            str = str.substring(0, str.length() - 1);
        }

        return str;
    }

    public static String[] getStringArr(String str) {
        str = trim(str);
        if (str.endsWith(",")) {
            str = str.substring(0, str.length() - 1);
        }
        String sep = ",";
        if (str.indexOf(':') >= 0) {
            sep = ":";
        }
        return str.split(sep);
    }

    public static String[] getStringArr(String str, String sep) {
        str = trim(str);
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.split(sep);
    }

    public static long[] getLongArray(String str, String sep) {
        if (isEmpty(str)) {
            return new long[0];
        }
        String[] prop = getStringArr(str, sep);
        List<Long> tmp = new ArrayList<>();
        for (String s : prop) {
            try {
                if (isEmpty(s)) {
                    continue;
                }
                long r = Long.parseLong(s);
                tmp.add(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        long[] longs = new long[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            longs[i] = tmp.get(i);
        }
        return longs;
    }

    public static int[] getIntArray(String str, String sep) {
        if (isEmpty(str)) {
            return new int[0];
        }
        String[] prop = getStringArr(str, sep);
        List<Integer> tmp = new ArrayList<Integer>();
        for (String s : prop) {
            try {
                if (isEmpty(s)) {
                    continue;
                }
                int r = Integer.parseInt(s);
                tmp.add(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        int[] ints = new int[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            ints[i] = tmp.get(i);
        }
        return ints;
    }

    public static List<Integer> getIntList(String str, String sep) {
        List<Integer> tmp = new ArrayList<Integer>();
        if (str == null || str.trim().equals("")) {
            return tmp;
        }
        String[] prop = getStringArr(str, sep);
        for (String s : prop) {
            try {
                if (isEmpty(s)) {
                    continue;
                }
                int r = Integer.parseInt(s);
                tmp.add(r);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return tmp;
    }

    /**
     * 判断是否含有特殊字符
     * 空格、\r、\n、\t
     */
    public static boolean isWhitespace(String str) {
        if (str == null) {
            return false;
        }
        Pattern p = Pattern.compile("\\s+|\\t|\\r|\\n");
        Matcher m = p.matcher(str);
        return m.find();
    }

}

