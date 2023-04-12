@extends('adminlte::page')
<!-- page title -->
@section('title', 'Create and Update Areas ' . Config::get('adminlte.title'))

@section('content_header')
    <h1>Areas</h1>
@stop

@section('content')
    {{--Show message if any--}}
    @include('layouts.flash-message')

    <div class="card">
        <div class="card-header">
            <h3 class="card-title">Add or Update</h3>
        </div>

        {{ Form::open(array('url' => route($data->form_action), 'method' => 'POST','autocomplete' => 'off', 'files' => true, 'id' => 'areaId')) }}
        {{ Form::hidden('id', $data->id, array('id' => 'id')) }}

        <div class="card-body">

            <div class="form-group row">
                <div class="col-sm-2 col-form-label">
                    <strong class="field-title">Name</strong>
                </div>
                <div class="col-sm-10 col-content">
                    {{ Form::text('name', $data->name, array('class' => 'form-control', 'required')) }}
                    <small class="form-text text-muted">
                        <i class="fa fa-question-circle" aria-hidden="true"></i> Area name.
                    </small>
                </div>
            </div>
 	 	 	      <div class="form-group row">
                <div class="col-sm-2 col-form-label">
                    <strong class="field-title">Address</strong>
                </div>
                <div class="col-sm-10 col-content">
                    {{ Form::text('address', $data->address, array('class' => 'form-control', 'required')) }}
                    <small class="form-text text-muted">
                        <i class="fa fa-question-circle" aria-hidden="true"></i> Area address.
                    </small>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-sm-2 col-form-label">
                    <strong class="field-title">Draw Area</strong>
                </div>
                <div class="col-sm-10 col-content">
                    <div id="map-canvas"></div>
                    <textarea id="info" class="hide"></textarea>
                    <br>
                    <h5 style="color: red;"><b><i class="fa fa-question-circle" aria-hidden="true"></i> Note</b></h5>
                    <hr>
                    <p><b>This map for development only. To fix this please add <a href="https://developers.google.com/maps/documentation/javascript/error-messages?utm_source=maps_js&utm_medium=degraded&utm_campaign=billing#api-key-and-billing-errors"> Google Maps key.</a></b><br>
                        To add Google API key:
                    <ol>
                        <li>Please insert it in resources/view/Backend/area/form.blade.php</li>
                        <li>Add key after '&key=' -> https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry,drawing&ext=.js&key=Add the API here</li>
                    </ol>
                    </p>
                </div>
            </div>
        </div>

        <div class="card-footer">
            <div id="form-button">
                <div class="col-sm-12 text-center top20">
                    <button id="saveLocation" type="button" class="btn btn-primary">{{ $data->button_text }}</button>
                </div>
            </div>
        </div>
        {{ Form::close() }}
    </div>

    <!-- /.card -->
    </div>
    <!-- /.row -->
    <!-- /.content -->
@stop

@section('css')

@stop

@section('js')
    <script>var typePage = "{{ $data->page_type }}";</script>
    <script src="https://maps.googleapis.com/maps/api/js?sensor=false&libraries=geometry,drawing&ext=.js&key="></script>
    <script src="{{ asset('js/backend/areas/form.js'). '?v=' . rand(99999,999999) }}"></script>
@stop
