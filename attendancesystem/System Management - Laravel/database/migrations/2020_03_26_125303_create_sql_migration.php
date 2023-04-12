<?php

use Illuminate\Database\Migrations\Migration;

class CreateSqlMigration extends Migration
{
    /**
     * Run the migrations.
     * Delete all table -> php artisan migrate:fresh
     *
     * @return void
     */
    public function up()
    {
        $path = storage_path('sql_dump/sql_file.sql');
        $sql = file_get_contents($path);
        DB::unprepared($sql);
    }
}
