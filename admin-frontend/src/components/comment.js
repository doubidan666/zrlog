import React from "react";
import {Table} from "antd";
import {BaseTableComponent} from "./base-table-component";
import Spin from "antd/lib/spin";
import Title from "antd/es/typography/Title";
import Divider from "antd/es/divider";
import Popconfirm from "antd/es/popconfirm";
import {DeleteOutlined} from "@ant-design/icons";


export class Comment extends BaseTableComponent {

    initState() {
        return {
            columns: [
                {
                    title: 'ID',
                    dataIndex: 'id',
                    key: 'id'
                },
                {
                    title: '',
                    dataIndex: 'id',
                    key: 'delete',
                    render: (text, record) =>
                        this.state.rows.length >= 1 ? (
                            <div style={{color: "red"}}>
                                <Popconfirm title="Sure to delete?"
                                            onConfirm={() => this.handleDelete(record.id)}>
                                    <DeleteOutlined/>
                                </Popconfirm>
                            </div>
                        ) : null,
                },
                {
                    title: '内容',
                    dataIndex: 'userComment',
                    key: 'userComment',
                    width: "200px"
                },
                {
                    title: '昵称',
                    dataIndex: 'userName',
                    key: 'userName',
                },
                {
                    title: '评论者主页',
                    key: 'userHome',
                    dataIndex: 'userHome'
                },
                {
                    title: 'IP',
                    key: 'ip',
                    dataIndex: 'ip'
                },
                {
                    title: '邮箱',
                    key: 'userMail',
                    dataIndex: 'userMail'
                },
                {
                    title: '评论时间',
                    key: 'commTime',
                    dataIndex: 'commTime'
                },
                {
                    title: '浏览',
                    key: 'view',
                    dataIndex: 'view'
                },
            ]
        }
    }

    getDeleteApiUri() {
        return "/api/admin/comment/delete"
    }

    getDataApiUri() {
        return "/api/admin/comment"
    }

    getSecondTitle() {
        return this.state.res['admin.comment.manage'];
    }

    render() {

        const {rows, pagination, tableLoading} = this.state;


        return (
            <Spin delay={this.getSpinDelayTime()} spinning={tableLoading}>
                <Title className='page-header' level={3}>{this.getSecondTitle()}</Title>
                <Divider/>
                <Table onChange={this.onShowSizeChange} columns={this.state.columns} pagination={pagination} dataSource={rows}/>
            </Spin>
        )
    }
}