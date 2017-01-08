package com.concavenp.artistrymuse.model;

/**
 * Created by dave on 12/22/2016.
 */

public class ProjectResponseHit {

    public String _id;
    public String _index;
    public float _score;
    public Project _source;
    public String _type;

    public ProjectResponseHit() {
        // TODO: comment fix - Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_index() {
        return _index;
    }

    public void set_index(String _index) {
        this._index = _index;
    }

    public float get_score() {
        return _score;
    }

    public void set_score(float _score) {
        this._score = _score;
    }

    public Project get_source() {
        return _source;
    }

    public void set_source(Project _source) {
        this._source = _source;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }
}
