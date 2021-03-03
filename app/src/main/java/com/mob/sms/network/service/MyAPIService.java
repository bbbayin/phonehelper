package com.mob.sms.network.service;

import com.mob.sms.network.bean.AuthoLoginBean;
import com.mob.sms.network.bean.BaseBean;
import com.mob.sms.network.bean.CallRecordBean;
import com.mob.sms.network.bean.EnterpriseBean;
import com.mob.sms.network.bean.FeedbackDetailBean;
import com.mob.sms.network.bean.HistoryFeedBackBean;
import com.mob.sms.network.bean.LoginBean;
import com.mob.sms.network.bean.OnlineContactBean;
import com.mob.sms.network.bean.OrderBean;
import com.mob.sms.network.bean.OrderHistoryBean;
import com.mob.sms.network.bean.QuestionBean;
import com.mob.sms.network.bean.QuestionDetailBean;
import com.mob.sms.network.bean.RecordBean;
import com.mob.sms.network.bean.RuleBean;
import com.mob.sms.network.bean.ShareBean;
import com.mob.sms.network.bean.SmsRecordBean;
import com.mob.sms.network.bean.UploadBean;
import com.mob.sms.network.bean.UserInfoBean;
import com.mob.sms.network.bean.VersionBean;
import com.mob.sms.network.bean.VipBean;

import java.io.File;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface MyAPIService {
    //发送验证码
    @POST("prod-api/restApi/appUser/sendSms")
    Observable<BaseBean> sendSms(@Query("username") String username);

    //注册
    @POST("prod-api/restApi/appUser/accountRegister")
    Observable<BaseBean> register(@Query("code") String code, @Query("confirmPwd") String confirmPwd, @Query("originate") String originate,
                                  @Query("password") String password, @Query("username") String username);

    //忘记密码
    @POST("prod-api/restApi/appUser/forgetPwd")
    Observable<BaseBean> forgetPwd(@Query("code") String code, @Query("confirmPwd") String confirmPwd,
                                  @Query("password") String password, @Query("username") String username);

    //登录
    @POST("prod-api/restApi/appUser/accountLogin")
    Observable<LoginBean> login(@Query("username") String username, @Query("password") String password);

    //绑定手机号
    @POST("prod-api/restApi/appUser/bingUsername")
    Observable<BaseBean> bindMobile(@Query("Authorization") String Authorization, @Query("username") String username, @Query("code") String code);

    //注销
    @POST("prod-api/restApi/appUser/delUser")
    Observable<BaseBean> delUser(@Query("Authorization") String Authorization);

    //授权登录
    @POST("prod-api/restApi/appUser/authLogin")
    Observable<AuthoLoginBean> authLogin(@Query("avatar") String avatar, @Query("loginType") String loginType,
                                         @Query("nickName") String nickName, @Query("openid") String openid, @Query("originate") String originate);

    //获取常见问题
    @POST("prod-api/restApi/question/getQuestion")
    Observable<QuestionBean> getQuestion();

    //获取版本信息
    @POST("prod-api/restApi/version/getVersion")
    Observable<VersionBean> getVersion();

    //获取问题详情
    @GET("prod-api/restApi/question/detail/{id}")
    Observable<QuestionDetailBean> getQuestionDetail(@Path("id") int id);

    //相关协议
    @GET("prod-api/restApi/rule/getRule/{type}")
    Observable<RuleBean> getRule(@Path("type") int type);

    //获取升级版企业信息
    @POST("prod-api/restApi/enterprise/getInfo")
    Observable<EnterpriseBean> getEnterpriseInfo();

    //获取历史反馈
    @POST("prod-api/restApi/feedback/getHistory")
    Observable<HistoryFeedBackBean> getFeedback(@Header("Authorization") String Authorization, @Query("pageNum") int pageNum,
                                                @Query("pageSize") int pageSize);

    //增加反馈
    @POST("prod-api/restApi/feedback/add")
    Observable<BaseBean> addFeedback(@Header("Authorization") String Authorization, @Query("content") String content,
                                     @Query("image") String image, @Query("phone") String phone);

    //回复反馈
    @POST("prod-api/restApi/feedback/reply")
    Observable<BaseBean> replyFeedback(@Header("Authorization") String Authorization, @Query("content") String content,
                                     @Query("feedbackId") int feedbackId, @Query("img") String img);

    //反馈详情
    @GET("prod-api/restApi/feedback/detail/{id}")
    Observable<FeedbackDetailBean> getDetailFeedback(@Path("id") int id);

    //获取分享信息
    @POST("prod-api/restApi/share/getShare")
    Observable<ShareBean> getShare();

    //获取全部套餐
    @POST("prod-api/restApi/level/getAll")
    Observable<VipBean> getVip();

    //添加联系人
    @POST("prod-api/restApi/address/addList")
    Observable<BaseBean> addContact(@Header("Authorization") String Authorization, @Query("name") String name,
                                     @Query("tel") String tel);

    //获取联系人
    @POST("prod-api/restApi/address/getList")
    Observable<OnlineContactBean> getContacts(@Header("Authorization") String Authorization);

    //删除联系人
    @POST("prod-api/restApi/address/delete")
    Observable<BaseBean> deleteContacts(@Header("Authorization") String Authorization, @Query("ids") String ids);

    //获取短信记录
    @POST("prod-api/restApi/record/recordNote")
    Observable<SmsRecordBean> getSmsRecords(@Header("Authorization") String Authorization, @Query("pageNum") int pageNum,
                                            @Query("pageSize") int pageSize);

    //获取拨号记录
    @POST("prod-api/restApi/record/recordDial")
    Observable<CallRecordBean> getCallRecords(@Header("Authorization") String Authorization, @Query("pageNum") int pageNum,
                                              @Query("pageSize") int pageSize);

    //获取群拨记录
    @POST("prod-api/restApi/record/recordBatch")
    Observable<CallRecordBean> getPlCallRecords(@Header("Authorization") String Authorization, @Query("pageNum") int pageNum,
                                         @Query("pageSize") int pageSize);

    //保存记录
    @POST("prod-api/restApi/record/saveRecord")
    Observable<BaseBean> saveRecord(@Header("Authorization") String Authorization, @Query("allNum") int allNum,
                                             @Query("status") String status, @Query("successNum") int successNum,
                                             @Query("tels") String tels, @Query("type") int type);

    //保存短信记录
    @POST("prod-api/restApi/record/saveRecordNote")
    Observable<BaseBean> saveSmsRecord(@Header("Authorization") String Authorization, @Query("allNum") int allNum,
                                    @Query("batchSend") String batchSend, @Query("content") String content,
                                    @Query("createTime") String createTime, @Query("oneSend") String oneSend,
                                       @Query("sendNum") int sendNum, @Query("status") String status,
                                       @Query("successNum") int successNum, @Query("tels") String tels,
                                       @Query("timing") String timing);

    //保存拨号记录
    @POST("prod-api/restApi/record/saveRecordDial")
    Observable<BaseBean> saveCallRecord(@Header("Authorization") String Authorization, @Query("allNum") int allNum,
                                       @Query("createTime") String createTime, @Query("dialInterval") String dialInterval,
                                       @Query("dialNum") int dialNum, @Query("dialType") String dialType,
                                       @Query("onHang") String onHang, @Query("status") String status,
                                       @Query("successNum") int successNum, @Query("tels") String tels,
                                       @Query("timing") String timing, @Query("unHang") String unHang);

    //保存群拨记录
    @POST("prod-api/restApi/record/saveRecordBatch")
    Observable<BaseBean> savePlCallRecord(@Header("Authorization") String Authorization, @Query("allNum") int allNum,
                                        @Query("cardSet") String cardSet, @Query("createTime") String createTime,
                                        @Query("intervalSet") String intervalSet, @Query("onHang") String onHang,
                                          @Query("status") String status, @Query("successNum") int successNum,
                                          @Query("tels") String tels, @Query("unHang") String unHang);

    //删除记录
    @POST("prod-api/restApi/record/deleteRecord")
    Observable<BaseBean> deleteRecord(@Header("Authorization") String Authorization, @Query("ids") String ids);

    //删除短信记录
    @POST("prod-api/restApi/record/deleteRecordNote")
    Observable<BaseBean> deleteSmsRecord(@Header("Authorization") String Authorization, @Query("ids") String ids);

    //删除拨号记录
    @POST("prod-api/restApi/record/deleteRecordDial")
    Observable<BaseBean> deleteCallRecord(@Header("Authorization") String Authorization, @Query("ids") String ids);

    //删除群拨记录
    @POST("prod-api/restApi/record/deleteRecordBatch")
    Observable<BaseBean> deletePlCallRecord(@Header("Authorization") String Authorization, @Query("ids") String ids);

    @Multipart
    @POST("prod-api/restApi/appUser/upload")
    Observable<UploadBean> upload(@PartMap Map<String, RequestBody> maps);

    //修改用户信息
    @POST("prod-api/restApi/appUser/updateUser")
    Observable<BaseBean> updateUser(@Header("Authorization") String Authorization, @Query("avatar") String avatar,
                                    @Query("nickName") String nickName);

    //支付订单
    @POST("prod-api/restApi/pay/toPay")
    Observable<BaseBean> pay(@Header("Authorization") String Authorization, @Query("orderId") int orderId,
                                    @Query("payType") String payType);

    //创建订单
    @POST("prod-api/restApi/order/createOrder")
    Observable<OrderBean> createOrder(@Header("Authorization") String Authorization, @Query("relationId") int relationId);

    //获取用户信息
    @POST("prod-api/restApi/appUser/getUser")
    Observable<UserInfoBean> getUserInfo(@Header("Authorization") String Authorization);

    //消费记录
    @POST("prod-api/restApi/order/history")
    Observable<OrderHistoryBean> getOrderHistory(@Header("Authorization") String Authorization);

    //保存通话记录
    @POST("prod-api/restApi/call/saveLog")
    Observable<OrderHistoryBean> saveLog(@Header("Authorization") String Authorization, @Query("dialTime") String dialTime,
                                         @Query("name") String name, @Query("tel") String tel);

    //获取通话记录
    @POST("prod-api/restApi/call/list")
    Observable<OrderHistoryBean> getLog(@Header("Authorization") String Authorization, @Query("pageNum") int pageNum,
                                         @Query("pageSize") int pageSize);
}
