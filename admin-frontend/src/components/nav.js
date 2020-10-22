import React from "react";
import {Table} from "antd";
import {BaseTableComponent} from "./base-table-component";
import Spin from "antd/lib/spin";
import Title from "antd/es/typography/Title";
import Divider from "antd/es/divider";
import Popconfirm from "antd/es/popconfirm";
import {DeleteOutlined} from "@ant-design/icons";

export class Nav extends BaseTableComponent {

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
                    title: '链接',
                    dataIndex: 'url',
                    key: 'url',
                },
                {
                    title: '导航名称',
                    dataIndex: 'navName',
                    key: 'navName',
                },
                {
                    title: '排序',
                    key: 'sort',
                    dataIndex: 'sort'
                }
            ]
        }
    }

    getDataApiUri() {
        return "/api/admin/nav"
    }

    getDeleteApiUri() {
        return "/api/admin/nav/delete";
    }

    getSecondTitle() {
        return this.state.res['admin.nav.manage'];
    }

    render() {

        const {rows, pagination, tableLoading} = this.state;


        return (
            <Spin spinning={tableLoading}>
                <Title className='page-header' level={3}>{this.getSecondTitle()}</Title>
                <Divider/>
                <Table onChange={this.onShowSizeChange} columns={this.state.columns} pagination={pagination}
                       dataSource={rows}/>
            </Spin>
        )
    }
}