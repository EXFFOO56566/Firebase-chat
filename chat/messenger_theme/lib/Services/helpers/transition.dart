import 'dart:math' as math;

import 'package:flutter/material.dart';

class BooleanTween<T> extends StatefulWidget {
  ///It is an AnimatedBuilder.
  ///If it is TRUE, it will execute the Tween from begin to end
  ///(controller.forward()),
  ///if it is FALSE it will execute the Tween from end to begin (controller.reverse())
  const BooleanTween({
    Key? key,
    required this.animate,
    required this.builder,
    this.child,
    this.curve = Curves.linear,
    this.duration = const Duration(milliseconds: 200),
    this.reverseCurve,
    this.reverseDuration,
    required this.tween,
  }) : super(key: key);

  ///If it is **TRUE**, it will execute the Tween from begin to end.
  ///
  ///If it is **FALSE** it will execute the Tween from end to begin
  final bool animate;

  ///Called every time the animation changes value.
  ///Return a Widget and receive the interpolation value as a parameter.
  final ValueWidgetBuilder<T> builder;

  final Widget? child;

  /// It is the curve that will carry out the interpolation.
  final Curve curve;

  /// It is the time it takes to execute the animation from beginning to end or vice versa.

  final Duration duration;

  /// It is the curve that will carry out the interpolation.
  final Curve? reverseCurve;

  /// It is the time it takes to execute the animation from beginning to end or vice versa.
  final Duration? reverseDuration;

  /// A linear interpolation between a beginning and ending value.
  ///
  /// [Tween] is useful if you want to interpolate across a range.
  ///
  ///You should use `LerpTween()` instead `Tween<double>(begin: 0.0, end: 1.0)`
  final Tween<T> tween;

  @override
  _BooleanTweenState<T> createState() => _BooleanTweenState<T>();
}

class _BooleanTweenState<T> extends State<BooleanTween<T>>
    with SingleTickerProviderStateMixin {
  late Animation<T> _animation;
  late AnimationController _controller;

  @override
  void didUpdateWidget(BooleanTween oldWidget) {
    super.didUpdateWidget(oldWidget as BooleanTween<T>);
    if (!oldWidget.animate && widget.animate) {
      _controller.forward();
    } else if (oldWidget.animate && !widget.animate) {
      _controller.reverse();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  void initState() {
    _controller = AnimationController(
      value: widget.animate ? 1.0 : 0.0,
      vsync: this,
      duration: widget.duration,
      reverseDuration: widget.reverseDuration,
    );
    _animation = widget.tween.animate(CurvedAnimation(
      parent: _controller,
      curve: widget.curve,
      reverseCurve: widget.reverseCurve,
    ));
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) => widget.builder(
        context,
        _animation.value,
        child,
      ),
      child: widget.child,
    );
  }
}

class OpacityTransition extends StatefulWidget {
  /// It is a FadeTransition but this will be shown when receiving a Boolean value.
  const OpacityTransition({
    Key? key,
    required this.visible,
    required this.child,
    this.duration = const Duration(milliseconds: 200),
    this.curve = Curves.linear,
  }) : super(key: key);

  /// It is the child that will be affected by the SwipeTransition
  final Widget child;

  /// It is the curve that the SwipeTransition performs
  final Curve curve;

  /// Is the time it takes to make the transition.
  final Duration duration;

  /// If true, it will show the widget.
  /// If false, it will hide the widget.
  final bool visible;

  @override
  _OpacityTransitionState createState() => _OpacityTransitionState();
}

class _OpacityTransitionState extends State<OpacityTransition> {
  @override
  Widget build(BuildContext context) {
    return BooleanTween<double>(
      tween: LerpTween(),
      animate: widget.visible,
      curve: widget.curve,
      duration: widget.duration,
      builder: (_, opacity, child) => Opacity(
        opacity: opacity,
        child: opacity > 0.0 ? child : null,
      ),
      child: widget.child,
    );
  }
}

class SwipeTransition extends StatelessWidget {
  /// It is a type of transition very similar to SizeTransition.
  /// The SwipeTransition fixes the problem that arises in the SlideTransition since
  /// always hides the elements on the screen and not on the parent widget,
  /// that is, if you performed the effect inside a 100x100 container the child widget
  /// of the SwipeTransition would pop out of the container and be overexposed on top of its other widgets.
  const SwipeTransition({
    Key? key,
    this.axis = Axis.vertical,
    this.axisAlignment = -1.0,
    required this.child,
    this.clip = Clip.antiAlias,
    this.curve = Curves.ease,
    this.duration = const Duration(milliseconds: 200),
    this.reverseCurve,
    this.reverseDuration,
    required this.visible,
  }) : super(key: key);

  /// [Axis.horizontal] if [sizeFactor] modifies the width, otherwise
  /// [Axis.vertical].
  final Axis axis;

  /// Describes how to align the child along the axis that [sizeFactor] is
  /// modifying.
  ///
  /// A value of -1.0 indicates the top when [axis] is [Axis.vertical], and the
  /// start when [axis] is [Axis.horizontal]. The start is on the left when the
  /// text direction in effect is [TextDirection.ltr] and on the right when it
  /// is [TextDirection.rtl].
  ///
  /// A value of 1.0 indicates the bottom or end, depending upon the [axis].
  ///
  /// A value of 0.0 (the default) indicates the center for either [axis] value.
  final double axisAlignment;

  /// It is the child that will be affected by the SwipeTransition
  final Widget child;

  final Clip clip;

  /// It is the curve that the SwipeTransition performs
  final Curve curve;

  /// Is the time it takes to make the transition.
  final Duration duration;

  /// It is the curve that the SwipeTransition performs
  final Curve? reverseCurve;

  /// Is the time it takes to make the transition.
  final Duration? reverseDuration;

  /// If true, it will show the widget in its position.
  /// If false, it will hide the widget.
  final bool visible;

  @override
  Widget build(BuildContext context) {
    final swipper = BooleanTween<double>(
      tween: LerpTween(),
      curve: curve,
      animate: visible,
      duration: duration,
      reverseCurve: reverseCurve,
      reverseDuration: reverseDuration,
      builder: (_, lerp, ___) => AlignFactor(
        axisAlignment: axisAlignment,
        lerp: lerp,
        axis: axis,
        child: child,
      ),
    );
    return clip == Clip.none
        ? swipper
        : ClipRRect(clipBehavior: clip, child: swipper);
  }
}

class AlignFactor extends StatelessWidget {
  const AlignFactor({
    Key? key,
    required this.lerp,
    this.axis = Axis.vertical,
    this.axisAlignment = -1.0,
    required this.child,
  }) : super(key: key);

  final Axis axis;
  final double axisAlignment;
  final Widget child;
  final double lerp;

  @override
  Widget build(BuildContext context) {
    final AlignmentDirectional alignment;
    if (axis == Axis.vertical) {
      alignment = AlignmentDirectional(-1.0, axisAlignment);
    } else {
      alignment = AlignmentDirectional(axisAlignment, -1.0);
    }

    return Align(
      alignment: alignment,
      heightFactor: axis == Axis.vertical ? math.max(lerp, 0.0) : null,
      widthFactor: axis == Axis.horizontal ? math.max(lerp, 0.0) : null,
      child: child,
    );
  }
}

class TranslateTransition extends StatelessWidget {
  /// It is a type of transition very similar to SlideTransition.
  const TranslateTransition({
    Key? key,
    this.begin = const Offset(0, 1),
    required this.child,
    this.curve = Curves.ease,
    this.duration = const Duration(milliseconds: 200),
    this.end = Offset.zero,
    this.textDirection,
    this.transformHitTests = true,
    required this.visible,
  }) : super(key: key);

  /// If true, it will show the widget in its position.
  /// If false, it will hide the widget.
  final Offset begin;

  /// It is the child that will be affected by the SwipeTransition
  final Widget child;

  /// It is the curve that the SwipeTransition performs
  final Curve curve;

  /// Is the time it takes to make the transition.
  final Duration duration;

  final Offset end;

  /// The direction to use for the x offset described by the [position].
  ///
  /// If [textDirection] is null, the x offset is applied in the coordinate
  /// system of the canvas (so positive x offsets move the child towards the
  /// right).
  ///
  /// If [textDirection] is [TextDirection.rtl], the x offset is applied in the
  /// reading direction such that x offsets move the child towards the left.
  ///
  /// If [textDirection] is [TextDirection.ltr], the x offset is applied in the
  /// reading direction such that x offsets move the child towards the right.
  final TextDirection? textDirection;

  /// Whether hit testing should be affected by the slide animation.
  ///
  /// If false, hit testing will proceed as if the child was not translated at
  /// all. Setting this value to false is useful for fast animations where you
  /// expect the user to commonly interact with the child widget in its final
  /// location and you want the user to benefit from "muscle memory".
  final bool transformHitTests;

  /// If true, it will show the widget in its position.
  /// If false, it will hide the widget.
  final bool visible;

  @override
  Widget build(BuildContext context) {
    return BooleanTween<Offset>(
      tween: Tween(begin: begin, end: end),
      curve: curve,
      animate: visible,
      duration: duration,
      builder: (_, offset, ___) {
        return FractionalTranslation(
          translation: textDirection == TextDirection.rtl
              ? Offset(-offset.dx, offset.dy)
              : offset,
          transformHitTests: transformHitTests,
          child: child,
        );
      },
    );
  }
}

class LerpTween extends Tween<double> {
  LerpTween() : super(begin: 0.0, end: 1.0);
}
