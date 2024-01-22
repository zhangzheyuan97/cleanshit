package com.meritdata.dam.datapacket.plan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @Author fanpeng
 * @Date 2023/4/11
 * @Describe 时间工具类
 */
public class DateUtils {
    private static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public final static String YYYY = "yyyy";

    public final static String MM = "MM";

    public final static String DD = "dd";

    public final static String YYYY_MM_DD = "yyyy-MM-dd";

    public final static String MYSQL_YYYY_MM_DD = "%Y-%m-%d";

    public final static String YYYY_MM = "yyyy-MM";

    public final static String HH_MM_SS = "HH:mm:ss";

    public final static String HH_MM = "HH:mm";

    public final static String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public final static String YYYYMMDDHHMM = "yyyyMMddHHmm";
    public final static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";


    /**
     * 构造函数
     */
    public DateUtils() {
    }

    /**
     * 日期格式化－将<code>Date</code>类型的日期格式化为<code>String</code>型
     *
     * @param date    待格式化的日期
     * @param pattern 时间样式
     * @return 一个被格式化了的<code>String</code>日期
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            return getFormatter(pattern).format(date);
        }
    }

    /**
     * java.util.Date年月日日期格式转成java.sql.Date对应日期格式
     *
     * @return java.sql.Date
     * @author:高伟朋
     * @date:2009-4-29
     */
    public static java.sql.Date toSqlYMDDate() {
        try {
            java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
            return d;
        } catch (Exception ex) {
            logger.error("日期格式转换出错，错误信息为：" + ex.getMessage());
            return null;
        }

    }

    /**
     * 默认为yyyy-MM-dd的格式化
     *
     * @param date
     * @return
     * @Fixed by luyz
     */
    public static String format(Date date) {
        if (date == null) {
            return "";
        } else {
            return getFormatter("yyyy-MM-dd").format(date);
        }
    }

    /**
     * 日期解析－将<code>String</code>类型的日期解析为<code>Date</code>型
     * <p>
     * 待格式化的日期
     *
     * @param pattern 日期样式
     *                如果所给的字符串不能被解析成一个日期
     * @return 一个被格式化了的<code>Date</code>日期
     */
    public static Date parse(String strDate, String pattern) throws ParseException {
        try {
            return getFormatter(pattern).parse(strDate);
        } catch (ParseException pe) {
            throw new ParseException("Method parse in Class DateUtils  err: parse strDate fail.", pe.getErrorOffset());
        }
    }

    /**
     * 默认为yyyy-MM-dd格式的解析
     *
     * @param strDate
     * @return
     * @throws ParseException
     * @Fixed by luyz
     */
    public static Date parse(String strDate) throws ParseException {
        try {
            return getFormatter("yyyy-MM-dd").parse(strDate);
        } catch (ParseException pe) {
            throw new ParseException("Method parse in Class DateUtils  err: parse strDate fail.", pe.getErrorOffset());
        }
    }

    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>Date</code>型日期
     */
    public static synchronized Date getCurrDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * 获取当前日期
     *
     * @return 一个包含年月日的<code>String</code>型日期，但不包含时分秒。yyyy-mm-dd
     */
    public static String getCurrDateStr() {
        return format(getCurrDate(), YYYY_MM_DD);
    }

    /**
     * 获取当前时间
     *
     * @return 一个包含时分秒的<code>String</code>型日期。hh:mm:ss
     */
    public static String getCurrTimeStr() {
        return format(getCurrDate(), HH_MM_SS);
    }

    /**
     * 获得给定时间的时分秒
     *
     * @param date
     * @return
     */
    public static String getCurrTimeStr(Date date) {
        return format(date, HH_MM_SS);
    }

    /**
     * 获得小时和分钟
     *
     * @return
     */
    public static String getCurrHMStr() {
        return format(getCurrDate(), HH_MM);
    }

    /**
     * 获取当前完整时间,样式: yyyy－MM－dd hh:mm:ss
     *
     * @return 一个包含年月日时分秒的<code>String</code>型日期。yyyy-MM-dd hh:mm:ss
     */
    public static String getCurrDateTimeStr() {
        return format(getCurrDate(), YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获得给定时间的年月日时分秒
     *
     * @param date
     * @return
     */
    public static String getCurrDateTimeStr(Date date) {
        return format(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 获取当前时间,不要秒（此处依据安阳局变电运行管理需求）,样式: yyyy－MM－dd hh:mm
     *
     * @return 一个包含年月日时分秒的<code>String</code>型日期。yyyy-MM-dd hh:mm
     */
    public static String getCurrDateTimeStr0() {
        return format(getCurrDate(), YYYY_MM_DD_HH_MM);
    }

    /**
     * 获取当前年分 样式：yyyy
     *
     * @return 当前年分
     */
    public static String getYear() {
        return format(getCurrDate(), YYYY);
    }

    /**
     * @return String 当前YYYY-MM时间
     * @function:获取当前时间，并格式化成YYYY-MM
     * @author:高伟朋
     * @date:2009-3-24
     * @desc:进行详细描述
     */
    public static String getYMDate() {
        return format(getCurrDate(), YYYY_MM);
    }

    /**
     * 获取当前月分 样式：MM
     *
     * @return 当前月分
     */
    public static String getMonth() {
        return format(getCurrDate(), MM);
    }

    /**
     * 获取当前日期号 样式：dd
     *
     * @return 当前日期号
     */
    public static String getDay() {
        return format(getCurrDate(), DD);
    }

    /**
     * 获取日期样式：dd add by gaowp at 2009
     *
     * @return 当前日期号
     */
    public static String getDay(Date d) {
        return format(d, DD);
    }

    /**
     * 按给定日期样式判断给定字符串是否为合法日期数据
     *
     * @param strDate 要判断的日期
     * @param pattern 日期样式
     * @return true 如果是，否则返回false
     */
    public static boolean isDate(String strDate, String pattern) {
        try {
            parse(strDate, pattern);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式年份（格式：YYYY）数据
     *
     * @param strDate 要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYY(String strDate) {
        try {
            parse(strDate, YYYY);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    public static boolean isYYYYMM(String strDate) {
        try {
            parse(strDate, YYYY_MM);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式的年月日（格式：yyyy-MM-dd）数据
     *
     * @param strDate 要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYYMMDD(String strDate) {
        try {
            parse(strDate, YYYY_MM_DD);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    /**
     * @param date 日期对象
     * @param flag 要截取的字符串个数
     * @return String 从0至flag的字串
     * @function:返回日期转换后的精确值
     * @author:高伟朋
     * @date:2009-3-23
     * @desc:进行详细描述
     */
    public static String toString(Date date, int flag) {
        try {
            return date.toString().substring(0, flag);
        } catch (Exception ex) {
            logger.error("日期格式转换出错，错误信息为：" + ex.getMessage());
            return date.toString();
        }
    }

    /**
     * 判断给定字符串是否为特定格式年月日时分秒（格式：yyyy-MM-dd HH:mm:ss）数据
     *
     * @param strDate 要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isYYYYMMDDHHMMSS(String strDate) {
        try {
            parse(strDate, YYYY_MM_DD_HH_MM_SS);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    public static boolean isYYYYMMDDHHMM(String strDate) {
        try {
            parse(strDate, YYYY_MM_DD_HH_MM);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    /**
     * 判断给定字符串是否为特定格式时分秒（格式：HH:mm:ss）数据
     *
     * @param strDate 要判断的日期
     * @return true 如果是，否则返回false
     */
    public static boolean isHHMMSS(String strDate) {
        try {
            parse(strDate, HH_MM_SS);
            return true;
        } catch (ParseException pe) {
            logger.error("日期格式转换出错，错误信息为：" + pe.getMessage());
            return false;
        }
    }

    /**
     * 获取一个简单的日期格式化对象
     *
     * @return 一个简单的日期格式化对象
     */
    private static SimpleDateFormat getFormatter(String parttern) {
        return new SimpleDateFormat(parttern);
    }

    /**
     * 获取给定日前的后intevalDay天的日期
     *
     * @param refenceDate 给定日期（格式为：yyyy-MM-dd）
     * @param intevalDays 间隔天数
     * @return 计算后的日期
     */
    public static String getNextDate(String refenceDate, int intevalDays) {
        try {
            return getNextDate(parse(refenceDate, YYYY_MM_DD), intevalDays);
        } catch (Exception ee) {
            logger.error("获取日期出错，错误信息为：" + ee.getMessage());
            return "";
        }
    }

    /**
     * 获取给定日前的后intevalDay天的日期
     *
     * @param refenceDate Date 给定日期
     * @param intevalDays int 间隔天数
     * @return String 计算后的日期
     */
    @SuppressWarnings("static-access")
    public static String getNextDate(Date refenceDate, int intevalDays) {
        try {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(refenceDate);
            calendar.set(calendar.DATE, calendar.get(calendar.DATE) + intevalDays);
            return format(calendar.getTime(), YYYY_MM_DD);
        } catch (Exception ee) {
            logger.error("获取日期出错，错误信息为：" + ee.getMessage());
            return "";
        }
    }

    public static long getIntevalDays(String startDate, String endDate) {
        try {
            return getIntevalDays(parse(startDate, YYYY_MM_DD), parse(endDate, YYYY_MM_DD));
        } catch (Exception ee) {
            logger.error("获取日期出错，错误信息为：" + ee.getMessage());
            return 0l;
        }
    }

    public static long getIntevalDays(Date startDate, Date endDate) {
        try {
            java.util.Calendar startCalendar = java.util.Calendar.getInstance();
            java.util.Calendar endCalendar = java.util.Calendar.getInstance();

            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();

            return (long) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception ee) {
            logger.error("获取日期出错，错误信息为：" + ee.getMessage());
            return 0l;
        }
    }

    public static double getIntevalHours(Date startDate, Date endDate) {
        try {
            java.util.Calendar startCalendar = java.util.Calendar.getInstance();
            java.util.Calendar endCalendar = java.util.Calendar.getInstance();

            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);
            long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();

            return (double) ((double) diff / (1000 * 60 * 60));
        } catch (Exception ee) {
            logger.error("获取日期出错，错误信息为：" + ee.getMessage());
            return 0.0;
        }
    }

    /**
     * 获取当前日期所在月的第一天的日期
     *
     * @param current
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Date getFirstDate(Date current) throws Exception {
        try {
            return new Date(current.getYear(), current.getMonth(), 1);
        } catch (Exception e) {
            logger.error("获取日期出错，错误信息为：" + e.getMessage());
            throw e;
        }
    }

    /**
     * 获取当前日期所在月的最后一天的日期
     *
     * @param current
     * @return
     * @throws Exception
     */
    public static Date getLastDate(Date current) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(current);
            c.add(Calendar.MONTH, 1);// 月份+1
            c.set(Calendar.DAY_OF_MONTH, 1);// 设置日期为1号
            c.add(Calendar.DATE, -1);// 获取current 所在月份的最后一天
            return c.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获取当前日期所在月上个月的第一天
     *
     * @param current
     * @return
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public static Date getLastMonth(Date current) throws Exception {
        try {
            Calendar calendar = Calendar.getInstance();
            Date date = new Date(current.getYear(), current.getMonth(), 1);
            calendar.setTime(date);
            calendar.set(Calendar.MONDAY, calendar.get(Calendar.MONDAY) - 1);
            return calendar.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获取所在日期去年的日期
     *
     * @param date
     * @return
     */
    public static Date getPreYearDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, year - 1);
        return calendar.getTime();
    }

    /**
     * 获取当前日期的星期
     *
     * @return
     * @throws Exception
     */
    public static String getWeek(Date date) throws Exception {
        try {
            final String dayNames[] = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            return dayNames[dayOfWeek - 1];
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获取星期几
     *
     * @return
     * @throws Exception
     */
    public static int getWeekDay(Date date) throws Exception {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.DAY_OF_WEEK);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获取2个日期之间间隔天数
     *
     * @param time1
     * @param time2
     * @return
     */
    public static long getQuot(String time1, String time2) {
        long quot = 0;

        try {
            Date date1 = parse(time1);
            Date date2 = parse(time2);
            quot = date1.getTime() - date2.getTime();
            quot = quot / 1000 / 60 / 60 / 24;
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return quot;
    }

    /**
     * 获取给定时间的前n天
     *
     * @return
     * @throws Exception
     */
    public static String getBeforeDayByDate(Date date, String pattern, int n) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(format(date, DD)) - n);
            return format(c.getTime(), pattern);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * <p>
     * 获得当前日期属于该月的第几周
     *
     * @param date
     * @return
     * @throws Exception
     * @author libaoxun, 2009-03-25
     */
    @SuppressWarnings("static-access")
    public static int getWeekNumber(Date date) throws Exception {
        int week = 0;
        try {
            Calendar calendar = Calendar.getInstance(); // 获得一个日历
            calendar.setTime(date); // 设置日历的时间
            calendar.setFirstDayOfWeek(Calendar.MONDAY); // 设置每周从星期一开始
            week = calendar.get(calendar.DAY_OF_WEEK_IN_MONTH); // 获得当前日期属于该月的第几周
            return week;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取本周一日期
     *
     * @param current 当前时间
     * @return
     * @throws Exception
     */
    public static Date getThisWeekMonday(Date current) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(current);
            c.setFirstDayOfWeek(Calendar.MONDAY);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);// // 本周1
            return c.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获得上周日时间
     *
     * @param current
     * @return
     * @throws Exception
     */
    public static Date getLastWeekSunday(Date current) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(getThisWeekMonday(current));
            c.setFirstDayOfWeek(Calendar.MONDAY);
            c.add(Calendar.DATE, -1);
            return c.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获得本周日的时间 liubin add
     *
     * @param current
     * @return
     * @throws Exception
     */
    public static Date getThisWeekSunday(Date current) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(getThisWeekMonday(current));
            c.setFirstDayOfWeek(Calendar.MONDAY);
            c.add(Calendar.DATE, 6);// 设置日期偏移量
            return c.getTime();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获得下个月第一天的日期
     */
    public static String getNextMonthFirstDay() {
        String str = "";
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(Calendar.MONTH, 1);// 加一个月
        lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        str = format(lastDate.getTime());
        return str;
    }

    // 获得下个月第一天的日期
    public static String getNextMonthFirst(String dateStr) {
        String str = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTime(parse(dateStr));
            lastDate.add(Calendar.MONTH, 1);// 减一个月
            lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
            str = sdf.format(lastDate.getTime());
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return str;
    }

    public static String toDateType(String dateType) {
        if ("yyyy".equals(dateType)) {
            dateType = "yyyy";
        } else if ("yy".equals(dateType)) {
            dateType = "yy";
        } else if ("yyyymm".equals(dateType)) {
            dateType = "yyyy-MM";
        } else if ("yymm".equals(dateType)) {
            dateType = "yy-MM";
        } else if ("yyyymmdd".equals(dateType)) {
            dateType = "yyyy-MM-dd";
        } else if ("yymmdd".equals(dateType)) {
            dateType = "yy-MM-dd";
        } else if ("mm".equals(dateType)) {
            dateType = "MM";
        } else if ("mmdd".equals(dateType)) {
            dateType = "MM-dd";
        } else if ("dd".equals(dateType)) {
            dateType = "dd";
        } else if ("yyyymmddhhmmss".equals(dateType)) {
            dateType = "yyyy-MM-dd HH:mm:ss";
        }
        return dateType;
    }

    /**
     * @param year
     * @param month
     * @return
     * @CreateAuthor: fanpeng
     * @CreateRemark:根据年月获取月份天数
     */
    public static Integer getDaysByYearMonth(Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, 1);
        calendar.roll(Calendar.DATE, -1);
        int maxDate = calendar.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * @CreateAuthor: fanpeng
     * @CreateRemark:获取年月日时间
     */
    public static Date getCurrDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        Date date = DateUtils.getCurrDate();
        try {
            date = sdf.parse(DateUtils.getCurrDateTimeStr());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return date;
    }

    /**
     * 获取当前日期所在月的天数
     *
     * @return
     * @throws Exception
     */
    public static int getMonthDays() throws Exception {
        try {
            int days = 0;
            Date firstDay = getFirstDate(new Date());
            Date lastDay = getLastDate(new Date());
            long b = getIntevalDays(firstDay, lastDay);
            days = (int) b + 1;
            return days;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * 获取传入日期的当月第一天
     *
     * @param date
     * @return
     */
    public static Date getMonthFirstDay(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        //获得本月第一天
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return sdf.parse(sdf.format(calendar.getTime()));
    }

    /**
     * 获取传入日期的当月最后一天
     *
     * @param date
     * @return
     */
    public static Date getMonthLastDay(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        //获得本月最后一天
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 获取传入日期的下个月第一天
     *
     * @param date
     * @return
     */
    public static Date getNextMonthFirstDay(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        //获得本月最后一天
        //月份+1
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return sdf.parse(sdf.format(calendar.getTime()));
    }


    public static String getCurrentDateTimeStr() {
        return format(getCurrDate(), YYYYMMDDHHMM);
    }

    public static String getCurrentDateTime(){
        return format(getCurrDate(),YYYYMMDDHHMMSS);
    }
}
