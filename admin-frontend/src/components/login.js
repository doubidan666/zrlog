import React from 'react'
import {Button, Form, Input, Layout} from 'antd';
import Card from "antd/es/card";
import axios from "axios";
import {message} from "antd/es";
import Spin from "antd/es/spin";
import {BaseResourceComponent} from "./base-resource-component";

const md5 = require('md5');

const layout = {
    labelCol: {span: 10},
    wrapperCol: {span: 8},
};
const tailLayout = {
    wrapperCol: {offset: 8, span: 8},
};

const {Content, Footer} = Layout;

export class Login extends BaseResourceComponent {

    loginFrom = React.createRef();

    setValue(changedValues) {
        this.loginFrom.current.setFieldsValue(changedValues);
    }

    onFinish(allValues) {
        const loginForm = {
            "userName": allValues.userName,
            "password": md5(allValues.password),
            "https": window.location.protocol === "https:"
        };
        axios.post("/api/admin/login", JSON.stringify(loginForm)).then(({data}) => {
            if (data.error) {
                message.error(data.message);
            } else {
                const query = new URLSearchParams(this.props.location.search);
                if (query.get("redirectFrom") !== null && query.get("redirectFrom") !== '') {
                    window.location.href = query.get("redirectFrom");
                } else {
                    window.location.href = window.location.protocol + "//" + window.location.host + "/admin/index";
                }
            }
        })
    };

    getSecondTitle() {
        return this.state.res.userNameAndPassword;
    }

    render() {
        return (
            <Spin spinning={this.state.resLoading}>
                <Layout>
                    <Content>
                        <Card className='login-container' style={{textAlign: "center"}} title={this.getSecondTitle()}>
                            <Form
                                ref={this.loginFrom}
                                {...layout}
                                onFinish={(values) => this.onFinish(values)}
                                onValuesChange={(k, v) => this.setValue(k, v)}
                            >
                                <Form.Item
                                    label={this.state.res.userName}
                                    name="userName"
                                    rules={[{required: true}]}
                                >
                                    <Input/>
                                </Form.Item>

                                <Form.Item
                                    label={this.state.res.password}
                                    name="password"
                                    rules={[{required: true}]}
                                >
                                    <Input.Password/>
                                </Form.Item>

                                <Form.Item {...tailLayout}>
                                    <Button type="primary" enterButton htmlType='submit'>
                                        {this.state.res.login}
                                    </Button>
                                </Form.Item>
                            </Form>
                        </Card>
                    </Content>
                    <Footer
                        style={{textAlign: 'center'}}>{this.state.res.copyrightCurrentYear} {this.state.res.websiteTitle}.
                        All Rights Reserved.</Footer>
                </Layout>
            </Spin>
        )
    }
}
