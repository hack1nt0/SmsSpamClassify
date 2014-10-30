使用说明：
	1.短信实体识别系统包括3个部分，jar包，配置文件（.config文件），词典目录（存放识别过程中，需要使用的资源）
	2.配置文件（.config文件）中需要配置词典目录的路径


调用示例：
		import java.util.ArrayList;
		
		import com.xiaomi.common.Log;
		import com.xiaomi.smsner.NumberRecognition;
		import com.xiaomi.smsner.RecognitionResult;
		
		
		public class Program {
			private static final String TAG = "Program";
			
			public static void main(String[] args){
		
				if(!NumberRecognition.initial()){
					return;
				}
        NumberRecognition.addRecognitionTask(EntityType.BankCardNumber);
        NumberRecognition.addRecognitionTask(EntityType.ExpressNumber);
        NumberRecognition.addRecognitionTask(EntityType.VerificationCode);
        NumberRecognition.addRecognitionTask(EntityType.PhoneNumber);
        NumberRecognition.addRecognitionTask(EntityType.URL);
        NumberRecognition.addRecognitionTask(EntityType.Time);

				for(int i=0;i<10;i++){
					String target="您好！快件688532824006单标地址错误且无法联系到您，无法派送，见字请致电4008-111-111处理。10:09【顺丰速运】";
					NumberRecognition nr=new NumberRecognition(target);
					ArrayList<RecognitionResult> recognitionResults=nr.recognize();
					for (RecognitionResult recognitionResult : recognitionResults) {
						Log.i(TAG, "实体开始位置："+recognitionResult.getStartPosition());
						Log.i(TAG, "实体结束位置："+recognitionResult.getEndPosition());
						Log.i(TAG, "规则化的结果："+recognitionResult.getRegularizationResult());
						Log.i(TAG, "实体的类型："+recognitionResult.getEntityType());
						Log.i(TAG, "实体的参数："+recognitionResult.getParameter());
						Log.i(TAG, "实体识别结果置信度："+recognitionResult.getConfidence());
					}
				}
			}
		}


识别说明：
	NumberRecognition.initial()为资源初始化函数。在系统启动时调用。用于将识别模块中所需要的资源加载到内存。
	
	识别调用示例：
		NumberRecognition nr=new NumberRecognition(String sMScontent);//sMScontent为短信文本内容
		ArrayList<RecognitionResult> recognitionResults=nr.recognize();
		识别结果按照顺序存放在ArrayList<RecognitionResult>中
		
			其中：RecognitionResult为识别结果类，
				getStartPosition()和getEndPosition()返回对应的实体在短信文本中的开始位置和结束位置（不包括结束位置）
				getRegularizationResult()返回规则化的识别结果。例如：电话4008-111-111，规则化的结果为：4008111111
				getEntityType()返回实体的类型，取值为：BankCardNumber,ExpressNumber,PhoneNumber,URL等。BankCardNumber表示银行卡号；ExpressNumber表示快递单号；PhoneNumber表示电话号码；URL为超链接
				getParameter()返回实体识别的参数。
						当getEntityType()为BankCardNumber时，getParameter()返回的值为银行的中文名（例如：中国工商银行）
						当getEntityType()为ExpressNumber时，getParameter()返回的值为快递公司的中文名（例如：顺丰、顺丰速运）
						当getEntityType()为PhoneNumber时，getParameter()返回的值为：服务电话、移动电话、座机电话
				getConfidence()返回实体识别结果的置信度，取值范围为[0,1]。值越大，准确性越强

