class WallpaperModel {
  String? path;
  String? name;
  bool? isSelected;
  List<WallpaperModel>? sublist;

  WallpaperModel({this.path, this.name, this.sublist, this.isSelected});

  List<WallpaperModel> wallpaperList() {
    List<WallpaperModel> wallpaperList = [];
    wallpaperList.add(WallpaperModel(name: "Bright", isSelected: false, path: "assets/default_wallpaper.png", sublist: brightWallpaperList()));
    wallpaperList.add(WallpaperModel(name: "Dark", isSelected: false, path: "assets/default_wallpaper.png", sublist: darkWallpaperList()));
    wallpaperList.add(WallpaperModel(name: "Solid colors", isSelected: false, path: "assets/default_wallpaper.png", sublist: solidWallpaperList()));
    return wallpaperList;
  }

  List<WallpaperModel> brightWallpaperList() {
    List<WallpaperModel> wallpaperList = [];
    wallpaperList.add(WallpaperModel(name: "Bright1", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper01.png"));
    wallpaperList.add(WallpaperModel(name: "Bright2", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper02.png"));
    wallpaperList.add(WallpaperModel(name: "Bright3", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper03.png"));
    wallpaperList.add(WallpaperModel(name: "Bright4", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper04.png"));
    wallpaperList.add(WallpaperModel(name: "Bright5", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper05.png"));
    wallpaperList.add(WallpaperModel(name: "Bright6", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper06.png"));
    wallpaperList.add(WallpaperModel(name: "Bright7", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper07.png"));
    wallpaperList.add(WallpaperModel(name: "Bright8", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper08.png"));
    wallpaperList.add(WallpaperModel(name: "Bright9", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper09.png"));
    wallpaperList.add(WallpaperModel(name: "Bright10", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper10.png"));
    wallpaperList.add(WallpaperModel(name: "Bright11", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper11.png"));
    wallpaperList.add(WallpaperModel(name: "Bright12", isSelected: false, path: "assets/Wallpaper/bright_wallpaper/bright_wallpaper12.png"));
    return wallpaperList;
  }

  List<WallpaperModel> darkWallpaperList() {
    List<WallpaperModel> wallpaperList = [];
    wallpaperList.add(WallpaperModel(name: "Dark1", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper01.png"));
    wallpaperList.add(WallpaperModel(name: "Dark2", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper02.png"));
    wallpaperList.add(WallpaperModel(name: "Dark3", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper03.png"));
    wallpaperList.add(WallpaperModel(name: "Dark4", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper04.png"));
    wallpaperList.add(WallpaperModel(name: "Dark5", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper05.png"));
    wallpaperList.add(WallpaperModel(name: "Dark6", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper06.png"));
    wallpaperList.add(WallpaperModel(name: "Dark7", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper07.png"));
    wallpaperList.add(WallpaperModel(name: "Dark8", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper08.png"));
    wallpaperList.add(WallpaperModel(name: "Dark9", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper09.png"));
    wallpaperList.add(WallpaperModel(name: "Dark10", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper10.png"));
    wallpaperList.add(WallpaperModel(name: "Dark11", isSelected: false, path: "assets/Wallpaper/dark_wallpaper/dark_wallpaper11.png"));
    return wallpaperList;
  }

  List<WallpaperModel> solidWallpaperList() {
    List<WallpaperModel> wallpaperList = [];
    wallpaperList.add(WallpaperModel(name: "Solid1", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper01.png"));
    wallpaperList.add(WallpaperModel(name: "Solid2", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper02.png"));
    wallpaperList.add(WallpaperModel(name: "Solid3", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper03.png"));
    wallpaperList.add(WallpaperModel(name: "Solid4", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper04.png"));
    wallpaperList.add(WallpaperModel(name: "Solid5", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper05.png"));
    wallpaperList.add(WallpaperModel(name: "Solid6", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper06.png"));
    wallpaperList.add(WallpaperModel(name: "Solid7", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper07.png"));
    wallpaperList.add(WallpaperModel(name: "Solid8", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper08.png"));
    wallpaperList.add(WallpaperModel(name: "Solid9", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper09.png"));
    wallpaperList.add(WallpaperModel(name: "Solid10", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper10.png"));
    wallpaperList.add(WallpaperModel(name: "Solid11", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper11.png"));
    wallpaperList.add(WallpaperModel(name: "Solid12", isSelected: false, path: "assets/Wallpaper/solid_wallpaper/solid_wallpaper11.png"));
    return wallpaperList;
  }
}
