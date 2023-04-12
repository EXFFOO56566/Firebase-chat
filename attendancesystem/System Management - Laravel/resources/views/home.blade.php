{{-- resources/views/admin/dashboard.blade.php --}}

@extends('adminlte::page')

@section('title', 'Dashboard  | ' . Config::get('adminlte.title'))

@section('content_header')
    <h1>Dashboard</h1>
@stop

@section('content')
    <div class="card">
        <div class="card-body">
            @if (session('status'))
                <div class="alert alert-success" role="alert">
                    {{ session('status') }}
                </div>
            @endif

            Hi, and Welcome!
        </div>
    </div>

    <div class="row">
        @if(Auth::user()->hasRole('administrator'))
        <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-warning">
                <div class="inner">
                    <h3>{{ $userCount }}</h3>

                    <p>Total Users</p>
                </div>
                <div class="icon">
                    <i class="fa fa-user-plus"></i>
                </div>
                <a href="{{ route('users') }}" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a>
            </div>
        </div>
        @endif

        @if(Auth::user()->hasRole('administrator'))
        <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-danger">
                <div class="inner">
                    <h3>{{ $attendanceLateToday }}</h3>

                    <p>Total Come late</p>
                </div>
                <div class="icon">
                    <i class="fa fa-clock"></i>
                </div>
                <a href="{{ route('attendances') }}" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a>
            </div>
        </div>
        @endif

        @if(Auth::user()->hasRole('administrator'))
        <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-primary">
                <div class="inner">
                    <h3>{{ $attendaceToday }}</h3>

                    <p>Total Attendances Today</p>
                </div>
                <div class="icon">
                    <i class="fa fa-database"></i>
                </div>
                <a href="{{ route('attendances') }}" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a>
            </div>
        </div>

        <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-gradient-gray">
                <div class="inner">
                    <h3>{{ $areaCount }}</h3>

                    <p>Total Areas</p>
                </div>
                <div class="icon">
                    <i class="fa fa-map-marked-alt"></i>
                </div>
                <a href="{{ route('areas') }}" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a>
            </div>
        </div>
        @endif

    </div>
@stop

@section('css')
@stop

@section('js')
@stop
