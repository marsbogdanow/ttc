'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const when = require('when');
const client = require('./client');
const follow = require('./follow'); // function to hop multiple links by "rel"
const stompClient = require('./websocket-listener');

const root = '/crudapi';

class App extends React.Component {

	constructor(props) {
		super(props);
        this.state = {pageSize: 2, attributes: [], words: [], links: [], page: 1, loggedInAppUser: this.props.loggedInAppUser};
		this.updatePageSize = this.updatePageSize.bind(this);
		this.onCreate = this.onCreate.bind(this);
		this.onNavigate = this.onNavigate.bind(this);
		this.onUpdate = this.onUpdate.bind(this);
		this.refreshCurrentPage = this.refreshCurrentPage.bind(this);
		this.refreshAndGoToLastPage = this.refreshAndGoToLastPage.bind(this);
	}

	// tag::websocket-handlers[]
	refreshAndGoToLastPage(message) {
		follow(client, root, [{
			rel: 'words',
			params: {size: this.state.pageSize}
		}]).done(response => {
			if (response.entity._links.last !== undefined) {
				this.onNavigate(response.entity._links.last.href);
			} else {
				this.onNavigate(response.entity._links.self.href);
			}
		})
	}

	refreshCurrentPage(message) {
		follow(client, root, [{
			rel: 'words',
			params: {
				size: this.state.pageSize,
				page: this.state.page.number
			}
		}]).then(wordCollection => {
			this.links = wordCollection.entity._links;
			this.page = wordCollection.entity.page;

			return wordCollection.entity._embedded.words.map(word => {
				return client({
					method: 'GET',
					path: word._links.self.href
				})
			});
		}).then(wordPromises => {
			return when.all(wordPromises);
		}).then(words => {
			this.setState({
				page: this.page,
				words: words,
				attributes: Object.keys(this.schema.properties),
				pageSize: this.state.pageSize,
				links: this.links
			});
		});
	}
	// end::websocket-handlers[]

    componentDidMount() {
        this.loadFromServer(this.state.pageSize);
        stompClient.register([
                    {route: '/topic/newWord', callback: this.refreshAndGoToLastPage},
                    {route: '/topic/updateWord', callback: this.refreshCurrentPage},
                    {route: '/topic/deleteWord', callback: this.refreshCurrentPage}
		]);
	}

	updatePageSize(pageSize) {
		if (pageSize !== this.state.pageSize) {
			this.loadFromServer(pageSize);
		}
	}

    loadFromServer(pageSize) {
        follow(client, root, [
            {rel: 'words', params: {size: pageSize}}]
        ).then(wordCollection => {
            return client({
                method: 'GET',
                path: wordCollection.entity._links.profile.href,
                headers: {'Accept': 'application/schema+json'}
            }).then(schema => {
                this.schema = schema.entity;
                this.links = wordCollection.entity._links;
                return wordCollection;
            });
        }).then(wordCollection => {
			this.page = wordCollection.entity.page;
            return wordCollection.entity._embedded.words.map(word =>
                client({
                    method: 'GET',
                    path: word._links.self.href
                })
            )
        }).then(wordPromises => {
        	return when.all(wordPromises);
        }).done(wordCollection => {
            this.setState({
				page: this.page,
                words: wordCollection,
                attributes: Object.keys(this.schema.properties),
                pageSize: pageSize,
                links: this.links});
        });
    }

	onCreate(newWord) {
	    const self = this;
		follow(client, root, ['words']).then(wordCollection => {
			return client({
				method: 'POST',
				path: wordCollection.entity._links.self.href,
				entity: newWord,
				headers: {'Content-Type': 'application/json'}
			})
		}).then(response => {
			return follow(client, root, [
				{rel: 'words', params: {'size': self.state.pageSize}}]);
		}).done(response => {
			if (typeof response.entity._links.last !== "undefined") {
				this.onNavigate(response.entity._links.last.href);
			} else {
				this.onNavigate(response.entity._links.self.href);
			}
		});
	}

    onUpdate(word, updatedWord) {
        updatedWord["appUser"] = word.entity.appUser
        client({
            method: 'PUT',
            path: word.entity._links.self.href,
            entity: updatedWord,
            headers: {
                'Content-Type': 'application/json',
                'If-Match': word.headers.Etag
            }
        }).done(response => {
            this.loadFromServer(this.state.pageSize);
        }, response => {
            if (response.status.code === 412) {
                alert('DENIED: Unable to update ' +
                    word.entity._links.self.href + '. Your copy is stale.');
            }
        });
    }
    
	onNavigate(navUri) {
    		client({
    			method: 'GET',
    			path: navUri
    		}).then(wordCollection => {
    			this.links = wordCollection.entity._links;
			    this.page = wordCollection.entity.page;

    			return wordCollection.entity._embedded.words.map(word =>
    					client({
    						method: 'GET',
    						path: word._links.self.href
    					})
    			);
    		}).then(wordPromises => {
    			return when.all(wordPromises);
    		}).done(words => {
    			this.setState({
				    page: this.page,
    				words: words,
    				attributes: Object.keys(this.schema.properties),
    				pageSize: this.state.pageSize,
    				links: this.links
    			});
    		});
    }

	render() {
		return (
            <div>
                <CreateDialog attributes={this.state.attributes} onCreate={this.onCreate}/>
                <WordList words={this.state.words}
                    page={this.state.page}
                    pageSize={this.state.pageSize}
                    updatePageSize={this.updatePageSize}
                    attributes={this.state.attributes}
                    links={this.state.links}
                    onNavigate={this.onNavigate}
                    onUpdate={this.onUpdate}
                    loggedInAppUser={this.state.loggedInAppUser}
                 />
            </div>
		)
	}
}

class WordList extends React.Component{
	constructor(props) {
		super(props);
		this.handleInput = this.handleInput.bind(this);
		this.handleNavFirst = this.handleNavFirst.bind(this);
		this.handleNavPrev = this.handleNavPrev.bind(this);
		this.handleNavNext = this.handleNavNext.bind(this);
		this.handleNavLast = this.handleNavLast.bind(this);
	}

	handleInput(e) {
		e.preventDefault();
		const pageSize = ReactDOM.findDOMNode(this.refs.pageSize).value;
		if (/^[0-9]+$/.test(pageSize)) {
			this.props.updatePageSize(pageSize);
		} else {
			ReactDOM.findDOMNode(this.refs.pageSize).value =
				pageSize.substring(0, pageSize.length - 1);
		}
	}

	handleNavFirst(e){
		e.preventDefault();
		this.props.onNavigate(this.props.links.first.href);
	}

	handleNavPrev(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.prev.href);
	}

	handleNavNext(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.next.href);
	}

	handleNavLast(e) {
		e.preventDefault();
		this.props.onNavigate(this.props.links.last.href);
	}

	render() {
        const pageInfo = this.props.page.hasOwnProperty("number") ?
        			<h3>Word - Page {this.props.page.number + 1} of {this.props.page.totalPages}</h3> : null;

		const words = this.props.words.map(word =>
			<Word   key={word.entity._links.self.href}
			        word={word}
			        attributes={this.props.attributes}
			        onUpdate={this.props.onUpdate}
			        loggedInAppUser={this.props.loggedInAppUser}
			/>
		);
		const navLinks = [];
		if ("first" in this.props.links) {
			navLinks.push(<button key="first" onClick={this.handleNavFirst}>&lt;&lt;</button>);
		}
		if ("prev" in this.props.links) {
			navLinks.push(<button key="prev" onClick={this.handleNavPrev}>&lt;</button>);
		}
		if ("next" in this.props.links) {
			navLinks.push(<button key="next" onClick={this.handleNavNext}>&gt;</button>);
		}
		if ("last" in this.props.links) {
			navLinks.push(<button key="last" onClick={this.handleNavLast}>&gt;&gt;</button>);
		}
		return (
		    <div>
			{pageInfo}
    		<input ref="pageSize" defaultValue={this.props.pageSize} onInput={this.handleInput}/>
			<table>
				<tbody>
					<tr>
						<th>Word to learn</th>
						<th>Meaning</th>
						<th>Note</th>
						<th>WordSet</th>
						<th>User</th>
					</tr>
					{words}
				</tbody>
			</table>
			<div>
				{navLinks}
			</div>
			</div>
		)
	}
}

class Word extends React.Component{
	render() {
		return (
			<tr>
				<td>{this.props.word.entity.wordToLearn}</td>
				<td>{this.props.word.entity.meaning}</td>
				<td>{this.props.word.entity.note}</td>
				<td>{this.props.word.entity.wordSet.name}</td>
				<td>{this.props.word.entity.appUser.email}</td>
                <td>
					<UpdateDialog word={this.props.word}
								  attributes={this.props.attributes}
								  wordSetId={this.props.word.entity.wordSet.id}
								  onUpdate={this.props.onUpdate}
								  loggedInAppUser={this.props.loggedInAppUser}
								  />
				</td>
			</tr>
		)
	}
}

class CreateDialog extends React.Component {

	constructor(props) {
		super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
	}

	handleSubmit(e) {
		e.preventDefault();
		const newWord = {};
		this.props.attributes.forEach(attribute => {
			newWord[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
		});
		this.props.onCreate(newWord);

		// clear out the dialog's inputs
		this.props.attributes.forEach(attribute => {
			ReactDOM.findDOMNode(this.refs[attribute]).value = '';
		});

		// Navigate away from the dialog to hide it.
		window.location = "#";
	}

	render() {
		const inputs = this.props.attributes.map(attribute =>
			<p key={attribute}>
				<input type="text" placeholder={attribute} ref={attribute} className="field"/>
			</p>
		);

		return (
			<div>
				<a href="#createWord">Create</a>

				<div id="createWord" className="modalDialog">
					<div>
						<a href="#" title="Close" className="close">X</a>

						<h2>Create new word set</h2>

						<form>
							{inputs}
							<button onClick={this.handleSubmit}>Create</button>
						</form>
					</div>
				</div>
			</div>
		)
	}

}

class UpdateDialog extends React.Component {
    constructor(props) {
        super(props);
		this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(e) {
            e.preventDefault();
            const updatedWord = {};
            this.props.attributes.forEach(attribute => {
                if (ReactDOM.findDOMNode(this.refs[attribute]) != null) {
                    updatedWord[attribute] = ReactDOM.findDOMNode(this.refs[attribute]).value.trim();
                } else {
                    updatedWord[attribute] = this.props.word.entity[attribute];
                }
            });
            this.props.onUpdate(this.props.word, updatedWord);
            window.location = "#";
    }

    render() {
            const inputs = this.props.attributes.map(attribute => {
                if (this.props.word.entity[attribute] == null || typeof(this.props.word.entity[attribute]) == "string") {
                    return (
                        <p key={this.props.word.entity[attribute]}>
                            <label for={attribute}>{attribute}:</label>
                            <input type="text" placeholder={attribute} id={attribute}
                                   defaultValue={this.props.word.entity[attribute]}
                                   ref={attribute} className="field"/>
                        </p>
                        )
                    }
                }
            );
            const dialogId = "updateWord-" + this.props.word.entity._links.self.href;

            return (
                <div>
                    <a href={"#" + dialogId}>Update</a>
                    <div id={dialogId} className="modalDialog">
                        <div>
                            <a href="#" title="Close" className="close">X</a>

                            <h2>Update an word</h2>

                            <form>
                                {inputs}
                                <button onClick={this.handleSubmit}>Update</button>
                            </form>
                        </div>
                    </div>
                </div>
            )
    }
}

ReactDOM.render(
	<App loggedInAppUser={document.getElementById('username').innerHTML }/>,
	document.getElementById('react')
)
