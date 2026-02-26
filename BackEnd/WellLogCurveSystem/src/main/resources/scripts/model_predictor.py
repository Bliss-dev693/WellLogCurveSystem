import sys
import json
import numpy as np
import joblib
import traceback
import os

# 设置系统路径，确保能找到模型文件
sys.path.append(os.path.dirname(os.path.abspath(__file__)))


def predict_cnl(input_data):
    """
    输入说明：
    input_data: 需要是包含5个时间步的二维数组，形状为(5,4)
    列顺序必须为 [AC, GR, RT, RXO]

    返回：
    预测的CNL值（浮点数）
    """
    try:
        # 加载模型和归一化器（使用绝对路径避免路径问题）
        version = '2'
        script_dir = os.path.dirname(os.path.abspath(__file__))
        #model_dir = os.path.join(script_dir, 'model_file')
        model_dir = r"D:\MyProgram\java\java-project\test02\src\main\resources\scripts\model_file"

        # 检查模型目录是否存在
        if not os.path.exists(model_dir):
            raise FileNotFoundError(f"模型目录不存在: {model_dir}")

        # 加载归一化器
        scaler_X_path = os.path.join(model_dir, f'scaler_X{version}.pkl')
        scaler_y_path = os.path.join(model_dir, f'scaler_y{version}.pkl')
        model_path = os.path.join(model_dir, f'model_Main{version}.h5')

        if not os.path.exists(scaler_X_path):
            raise FileNotFoundError(f"特征归一化器文件不存在: {scaler_X_path}")
        if not os.path.exists(scaler_y_path):
            raise FileNotFoundError(f"标签归一化器文件不存在: {scaler_y_path}")
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"模型文件不存在: {model_path}")

        scaler_X = joblib.load(scaler_X_path)
        scaler_y = joblib.load(scaler_y_path)

        # 数据预处理
        input_array = np.array(input_data, dtype=np.float64)
        if input_array.shape != (5, 4):
            raise ValueError(f"输入数据形状错误，期望(5,4)，实际{input_array.shape}")

        input_scaled = scaler_X.transform(input_array)
        input_reshaped = input_scaled.reshape(1, 5, 4)

        # 读取模型
        from tensorflow.keras.models import load_model
        model = load_model(model_path)

        # 进行预测
        predicted_scaled = model.predict(input_reshaped, verbose=0)

        # 反归一化
        predicted_cnl = scaler_y.inverse_transform(predicted_scaled)

        return float(predicted_cnl[0][0])

    except Exception as e:
        # 将异常信息输出到标准错误流
        error_msg = f"预测过程出错: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return None


def main():
    try:
        # 读取输入数据（优先命令行参数，其次标准输入）
        if len(sys.argv) > 1:
            input_json = sys.argv[1]
        else:
            input_json = sys.stdin.read().strip()

        if not input_json:
            raise ValueError("未接收到输入数据")

        # 解析输入数据
        request_data = json.loads(input_json)

        # 提取参数数据
        input_data = []
        for step in request_data:
            if 'data' not in step or 'parameters' not in step['data']:
                raise ValueError("输入数据格式错误，缺少data/parameters字段")

            params = step['data']['parameters']
            required_fields = ['AC', 'GR', 'RT', 'RXO']
            for field in required_fields:
                if field not in params:
                    raise ValueError(f"缺少必要参数: {field}")

            # 转换为浮点数
            data = [
                float(params['AC']),
                float(params['GR']),
                float(params['RT']),
                float(params['RXO'])
            ]
            input_data.append(data)

        # 验证输入数据长度
        if len(input_data) != 5:
            raise ValueError(f"时间步数量错误，期望5个，实际{len(input_data)}个")

        # 进行预测
        prediction = predict_cnl(input_data)

        # 构建返回结果
        if prediction is not None:
            result = {
                'status': 'success',
                'prediction': prediction
            }
        else:
            result = {
                'status': 'error',
                'message': '模型预测失败，请检查输入数据或模型文件'
            }

    except json.JSONDecodeError as e:
        result = {
            'status': 'error',
            'message': f'JSON解析错误: {str(e)}'
        }
    except ValueError as e:
        result = {
            'status': 'error',
            'message': f'输入数据验证失败: {str(e)}'
        }
    except Exception as e:
        result = {
            'status': 'error',
            'message': f'处理数据时出错: {str(e)}'
        }

    # 输出结果（确保是有效的JSON）
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()