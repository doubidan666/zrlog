import React from "react";

import {Input, Tag} from 'antd';
import {TweenOneGroup} from 'rc-tween-one';
import {PlusOutlined, TagOutlined} from '@ant-design/icons';
import {BaseResourceComponent} from "./base-resource-component";
import Title from "antd/es/typography/Title";


export class ArticleEditTag extends BaseResourceComponent {

    state = {
        keywords: this.props.keywords,
        allTags: this.props.allTags
    }

    initState() {
        return {
            inputVisible: false,
            inputValue: '',
            keywords: '',
            allTags: []
        }
    }

    handleClose = removedTag => {
        const tags = this.state.keywords.split(",").filter(tag => tag !== removedTag);
        console.info(tags.join(","));
        //this.props.tags = tags;
        this.setState({
            keywords: tags.join(",")
        })
    };

    showInput = () => {
        this.setState({inputVisible: true}, () => this.input.focus());
    };

    handleInputChange = e => {
        this.setState({inputValue: e.target.value});
    };

    handleInputConfirm = () => {
        const {inputValue} = this.state;
        let {keywords} = this.state;
        if (inputValue && keywords.indexOf(inputValue) === -1) {
            keywords = keywords += "," + inputValue;
        }
        console.log(keywords);
        this.setState({
            keywords: keywords,
            inputVisible: false,
            inputValue: '',
        });
    };

    saveInputRef = input => {
        this.input = input;
    };

    forMap = tag => {
        const tagElem = (
            <Tag
                color="#108ee9"
                closable
                onClose={e => {
                    e.preventDefault();
                    this.handleClose(tag);
                }}
            >
                {tag}
            </Tag>
        );
        return (
            <span key={tag} style={{display: 'inline-block'}}>
        {tagElem}
      </span>
        );
    };

    allTagsOnClick(e) {
        e.currentTarget.remove();
        let tags = this.state.keywords.split(",");
        tags.push(e.currentTarget.textContent);
        this.setState({
            keywords: tags.join(',')
        })
    }

    tagForMap = tag => {
        const tagElem = (
            <Tag icon={<TagOutlined/>} onClick={(e) => this.allTagsOnClick(e)} closable={false} color="#108ee9">
                {tag}
            </Tag>
        );
        return (
            <span key={"all-" + tag} style={{display: 'inline-block'}}>
        {tagElem}
      </span>
        );
    };

    render() {
        const {inputVisible, inputValue} = this.state;
        let tagChild;
        if (this.props.keywords !== undefined && this.props.keywords !== '') {
            if (this.state.keywords !== undefined) {
                console.info(this.state.keywords);
                //this.state.tags = this.props.keywords + "," + this.state.tags;
            } else {
                this.state.keywords = this.props.keywords;
            }
            let newTags = Array.from(new Set(this.state.keywords.split(",").filter(x => x !== '')));
            if (newTags.length > 0) {
                this.state.keywords = newTags.join(",");
                tagChild = newTags.map(this.forMap);
            } else {
                this.state.keywords = '';
                tagChild = [].map(this.forMap);
            }
        }
        const allTagChild = this.props.allTags.map(this.tagForMap);
        return (
            <>
                <Input id='keywords' value={this.state.keywords} hidden={true}/>
                <div style={{marginBottom: 16}}>
                    <TweenOneGroup
                        enter={{
                            scale: 0.8,
                            opacity: 0,
                            type: 'from',
                            duration: 100,
                            onComplete: e => {
                                e.target.style = '';
                            },
                        }}
                        leave={{opacity: 0, width: 0, scale: 0, duration: 200}}
                        appear={false}
                    >
                        {tagChild}
                    </TweenOneGroup>

                </div>
                {inputVisible && (
                    <Input
                        ref={this.saveInputRef}
                        type="text"
                        size="small"
                        style={{width: 78}}
                        value={inputValue}
                        onChange={this.handleInputChange}
                        onBlur={this.handleInputConfirm}
                        onPressEnter={this.handleInputConfirm}
                    />
                )}
                {!inputVisible && (
                    <>
                        <Tag onClick={this.showInput} className="site-tag-plus">
                            <PlusOutlined/> New Tag
                        </Tag>
                        <Title level={5} style={{paddingTop: "15px"}}>All Tags</Title>
                        <div style={{maxHeight: "120px", overflowY: "scroll"}}>
                            {allTagChild}
                        </div>
                    </>

                )}

            </>
        );
    }
}