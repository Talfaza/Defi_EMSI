// Basic Flutter widget test for DeFi Clinic Payment app
//
// This test verifies that the app builds and renders without errors.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:untitled/main.dart';

void main() {
  testWidgets('App renders without errors', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Verify that the app renders (MaterialApp exists)
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
